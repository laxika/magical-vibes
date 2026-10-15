package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.t.TezzeretBetrayerOfFlesh;
import com.github.laxika.magicalvibes.cards.w.WalkingSkyscraper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DokuchiSilencer.class, BearerOfMemory.class, TezzeretBetrayerOfFlesh.class, Mountain.class,
        WalkingSkyscraper.class})
class DokuchiSilencerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may discard a creature card to destroy a creature or planeswalker the damaged player controls")
    void discardsCreatureToDestroyCreatureOrPlaneswalker() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        Permanent enemyCreature = addCreatureReady(player2, new BearerOfMemory());
        Permanent enemyPlaneswalker = harness.addToBattlefieldAndReturn(player2, new TezzeretBetrayerOfFlesh());
        Permanent ownCreature = addCreatureReady(player1, new BearerOfMemory());
        harness.setHand(player1, List.of(new BearerOfMemory(), new Mountain()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice discardChoice = gd.interaction.activeInteraction(
                PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.PermanentChoice targetChoice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPermanentIds())
                .contains(enemyCreature.getId(), enemyPlaneswalker.getId())
                .doesNotContain(ownCreature.getId());
        harness.handlePermanentChosen(player1, enemyPlaneswalker.getId());
        harness.assertOnBattlefield(player2, "Tezzeret, Betrayer of Flesh");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bearer of Memory");
        harness.assertInGraveyard(player2, "Tezzeret, Betrayer of Flesh");
        harness.assertOnBattlefield(player2, "Bearer of Memory");
    }

    @Test
    @DisplayName("Declining the optional discard does not destroy anything")
    void decliningDiscardDoesNothing() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        addCreatureReady(player2, new BearerOfMemory());
        harness.setHand(player1, List.of(new BearerOfMemory()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Bearer of Memory");
        harness.assertInHand(player1, "Bearer of Memory");
    }

    @Test
    @DisplayName("Accepting the ability does nothing when the hand has no creature card")
    void noCreatureCardMeansNoFollowUp() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        addCreatureReady(player2, new BearerOfMemory());
        harness.setHand(player1, List.of(new Mountain()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Bearer of Memory");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Dokuchi Silencer onto the battlefield attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new BearerOfMemory());
        addCreatureReady(player2, new BearerOfMemory());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DokuchiSilencer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bearer of Memory");
        Permanent silencer = findPermanent(player1, "Dokuchi Silencer");
        assertThat(silencer.isTapped()).isTrue();
        assertThat(silencer.isAttacking()).isTrue();
        assertThat(silencer.getAttackTarget()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Discarding a creature cannot destroy an untapped creature with hexproof")
    void hexproofCreatureCannotBeDestroyed() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        harness.addToBattlefield(player2, new WalkingSkyscraper());
        harness.setHand(player1, List.of(new BearerOfMemory()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Bearer of Memory");
        harness.assertOnBattlefield(player2, "Walking Skyscraper");
    }

    @Test
    @DisplayName("A creature can be discarded even when the damaged player has no legal target")
    void mayDiscardWithNoTarget() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        harness.setHand(player1, List.of(new BearerOfMemory()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Bearer of Memory");
        harness.assertOnBattlefield(player1, "Dokuchi Silencer");
    }

    @Test
    @DisplayName("The reflexive ability can destroy a creature controlled by the damaged player")
    void destroysCreatureAfterDiscard() {
        Permanent silencer = addCreatureReady(player1, new DokuchiSilencer());
        silencer.setAttacking(true);
        Permanent target = addCreatureReady(player2, new BearerOfMemory());
        harness.setHand(player1, List.of(new BearerOfMemory()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, target.getId());
        harness.assertOnBattlefield(player2, "Bearer of Memory");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bearer of Memory");
        harness.assertInGraveyard(player2, "Bearer of Memory");
    }
}
