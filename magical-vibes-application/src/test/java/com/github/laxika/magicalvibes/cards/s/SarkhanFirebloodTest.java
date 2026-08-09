package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SarkhanFirebloodTest extends BaseCardTest {

    @Test
    @DisplayName("The first plus ability may discard a card and draw a card")
    void plusOneRummages() {
        Permanent sarkhan = addReadySarkhan(player1);
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The first plus ability can be declined")
    void plusOneCanBeDeclined() {
        Permanent sarkhan = addReadySarkhan(player1);
        Card card = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(card)));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("The second plus ability adds two individually chosen Dragon-spell mana")
    void plusOneAddsDragonSpellMana() {
        Permanent sarkhan = addReadySarkhan(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON),
                        ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaForColor(Set.of(CardSubtype.DRAGON),
                        ManaColor.BLUE)).isEqualTo(1);
        assertThat(sarkhan.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Dragon-spell mana can cast a Dragon spell")
    void dragonSpellManaCanCastDragonSpell() {
        addReadySarkhan(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Dragon-spell mana cannot cast a non-Dragon spell")
    void dragonSpellManaCannotCastNonDragonSpell() {
        addReadySarkhan(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "RED");
        harness.setHand(player1, List.of(new LightningBolt()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ultimate creates four 5/5 flying red Dragon tokens")
    void ultimateCreatesDragonTokens() {
        Permanent sarkhan = addReadySarkhan(player1);
        sarkhan.setCounterCount(CounterType.LOYALTY, 7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(4);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getName()).isEqualTo("Dragon");
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
            assertThat(token.getCard().getPower()).isEqualTo(5);
            assertThat(token.getCard().getToughness()).isEqualTo(5);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
        harness.assertNotOnBattlefield(player1, "Sarkhan, Fireblood");
    }

    private Permanent addReadySarkhan(Player player) {
        Permanent permanent = new Permanent(new SarkhanFireblood());
        permanent.setCounterCount(CounterType.LOYALTY, 3);
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return permanent;
    }
}
