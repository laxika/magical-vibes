package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZoralineCosmosCaller.class, GrizzlyBears.class, Forest.class, ThunderingGiant.class})
class ZoralineCosmosCallerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can pay mana and life to return a qualifying permanent with a finality counter")
    void etbReturnsPermanentWithFinalityCounter() {
        Card eligible = new GrizzlyBears();
        Card land = new Forest();
        Card tooExpensive = new ThunderingGiant();
        harness.setGraveyard(player1, List.of(eligible, land, tooExpensive));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanentByCardId(eligible.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player1, 18);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the ETB payment leaves the graveyard card and life total unchanged")
    void decliningEtbPaymentDoesNothing() {
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new ZoralineCosmosCaller()));
        addZoralineMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
    }

    @Test
    @DisplayName("A Bat attack gains 1 life")
    void batAttackGainsLife() {
        harness.setLife(player1, 10);
        addReadyZoraline(player1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    @DisplayName("Attacking Zoraline can pay mana and life to return a permanent")
    void attackReturnsPermanentWithFinalityCounter() {
        Card eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        addReadyZoraline(player1);
        addReturnMana();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanentByCardId(eligible.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertLife(player1, 18);
    }

    private void addZoralineMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addReturnMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addReadyZoraline(Player player) {
        Permanent permanent = new Permanent(new ZoralineCosmosCaller());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
