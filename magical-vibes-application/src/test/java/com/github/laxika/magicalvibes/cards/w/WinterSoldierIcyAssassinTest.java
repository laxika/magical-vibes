package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
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

@CardUsed({WinterSoldierIcyAssassin.class, LeoninScimitar.class, GrizzlyBears.class})
class WinterSoldierIcyAssassinTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard with a finality counter and can attach one Equipment")
    void returnsWithFinalityCounterAndAttachesEquipment() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanentByCardId(winterSoldier.getId());
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(equipment.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
    }

    @Test
    @DisplayName("Choosing among multiple Equipment attaches only the chosen one")
    void choosesOneEquipment() {
        Card winterSoldier = new WinterSoldierIcyAssassin();
        harness.setGraveyard(player1, List.of(winterSoldier));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent returned = findPermanentByCardId(winterSoldier.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(second.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(first.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("A finality counter exiles Winter Soldier instead of putting it into a graveyard")
    void finalityCounterExilesItInsteadOfDying() {
        Permanent winterSoldier = addReady(player1, new WinterSoldierIcyAssassin());
        winterSoldier.setCounterCount(CounterType.FINALITY, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, winterSoldier));

        harness.assertNotOnBattlefield(player1, "Winter Soldier, Icy Assassin");
        harness.assertNotInGraveyard(player1, "Winter Soldier, Icy Assassin");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(winterSoldier.getCard().getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
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
