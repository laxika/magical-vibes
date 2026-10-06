package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ChandraNovicePyromancer;
import com.github.laxika.magicalvibes.cards.e.EnsoulArtifact;
import com.github.laxika.magicalvibes.cards.l.Lifelink;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RetributiveWand.class, LlanowarElves.class, GrizzlyBears.class, Naturalize.class,
        EnsoulArtifact.class, Lifelink.class, ChandraNovicePyromancer.class})
class RetributiveWandTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability deals 1 damage to a target player")
    void activatedAbilityDealsDamageToPlayer() {
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new RetributiveWand());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(wand.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Activated ability deals 1 damage to a target creature")
    void activatedAbilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("When put into a graveyard from the battlefield, deals 5 damage to a target player")
    void graveyardTriggerDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new RetributiveWand());
        harness.setLife(player2, 20);
        destroyWandWithNaturalize();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("When put into a graveyard from the battlefield, deals 5 damage to a target creature")
    void graveyardTriggerDealsDamageToCreature() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyWandWithNaturalize();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Activated ability requires three mana")
    void activatedAbilityRequiresThreeMana() {
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new RetributiveWand());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wand.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Wand cannot activate again")
    void tappedWandCannotActivateAgain() {
        harness.addToBattlefield(player1, new RetributiveWand());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activated ability cannot target a noncreature artifact")
    void activatedAbilityRejectsNoncreatureArtifact() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent otherWand = harness.addToBattlefieldAndReturn(player2, new RetributiveWand());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherWand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard trigger uses the Wand's lifelink before it left the battlefield")
    void graveyardTriggerRetainsLifelink() {
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new RetributiveWand());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EnsoulArtifact(), new Lifelink()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, wand.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, wand.getId());
        harness.passBothPriorities();

        destroyWandWithNaturalize();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Activated ability removes one loyalty from a planeswalker")
    void activatedAbilityDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Chandra, Novice Pyromancer");
    }

    @Test
    @DisplayName("Graveyard trigger deals five damage to a planeswalker")
    void graveyardTriggerDamagesPlaneswalker() {
        harness.addToBattlefield(player1, new RetributiveWand());
        Permanent chandra = harness.enterBattlefieldAndReturn(player2, new ChandraNovicePyromancer());
        destroyWandWithNaturalize();

        harness.handlePermanentChosen(player1, chandra.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Chandra, Novice Pyromancer");
    }

    private void destroyWandWithNaturalize() {
        UUID wandId = harness.getPermanentId(player1, "Retributive Wand");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, wandId);
    }
}
