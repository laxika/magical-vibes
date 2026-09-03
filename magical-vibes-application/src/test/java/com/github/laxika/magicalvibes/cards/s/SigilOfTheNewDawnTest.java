package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SigilOfTheNewDawn.class, GrizzlyBears.class})
class SigilOfTheNewDawnTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{W} returns the creature to its owner's hand")
    void payingReturnsCreatureToHand() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        destroy(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the creature in its owner's graveyard")
    void decliningLeavesCreatureInGraveyard() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroy(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Triggers for a creature you own even if an opponent controls it")
    void triggersForOwnedCreatureControlledByOpponent() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        gd.stolenCreatures.put(bears.getId(), player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        destroy(bears);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not trigger for a creature owned by an opponent")
    void doesNotTriggerForOpponentOwnedCreature() {
        harness.addToBattlefield(player1, new SigilOfTheNewDawn());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        destroy(bears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void destroy(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
