package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InkEyesServantOfOni.class, GnarledMass.class, GodsEyeGateToTheReikai.class, TorrentOfStone.class})
class InkEyesServantOfOniTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller reanimate a creature from the damaged player's graveyard")
    void combatDamageReanimatesChosenCreature() {
        Card creature = new GnarledMass();
        harness.setGraveyard(player2, List.of(creature));
        attackWithInkEyesDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Gnarled Mass");
        harness.assertNotInGraveyard(player2, "Gnarled Mass");
        assertThat(findPermanent(player1, "Gnarled Mass").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining at resolution leaves the targeted creature in the graveyard")
    void decliningLeavesCreatureInGraveyard() {
        Card creature = new GnarledMass();
        harness.setGraveyard(player2, List.of(creature));
        attackWithInkEyesDealingDamage();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Gnarled Mass");
        harness.assertInGraveyard(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Only creature cards from the damaged player's graveyard are offered")
    void controllerOwnGraveyardIsNotOffered() {
        Card ownCreature = new GnarledMass();
        Card opponentCreature = new GnarledMass();
        Card opponentLand = new GodsEyeGateToTheReikai();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentLand, opponentCreature));
        attackWithInkEyesDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(opponentCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opponentCreature.getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentLand, opponentCreature);
    }

    @Test
    @DisplayName("{1}{B} grants Ink-Eyes a regeneration shield")
    void regenerationShieldIsGranted() {
        Permanent inkEyes = addCreatureReady(player1, new InkEyesServantOfOni());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(inkEyes.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ninjutsu returns an unblocked attacker and puts Ink-Eyes onto the battlefield tapped and attacking")
    void ninjutsuSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of()));

        harness.setHand(player1, List.of(new InkEyesServantOfOni()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gnarled Mass");
        Permanent inkEyes = findPermanent(player1, "Ink-Eyes, Servant of Oni");
        assertThat(inkEyes.isTapped()).isTrue();
        assertThat(inkEyes.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The trigger requires exactly one target even when its controller intends to decline")
    void targetIsMandatoryBeforeResolution() {
        harness.setGraveyard(player2, List.of(new GnarledMass()));
        attackWithInkEyesDealingDamage();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not reanimated")
    void removedTargetIsNotReanimated() {
        Card creature = new GnarledMass();
        harness.setGraveyard(player2, List.of(creature));
        attackWithInkEyesDealingDamage();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Gnarled Mass");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("No reanimation choice is offered when the damaged player has no creature cards")
    void noCreatureCardsMeansNoChoice() {
        harness.setGraveyard(player2, List.of(new GodsEyeGateToTheReikai()));
        attackWithInkEyesDealingDamage();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Gods' Eye, Gate to the Reikai");
        harness.assertNotOnBattlefield(player1, "Gods' Eye, Gate to the Reikai");
    }

    @Test
    @DisplayName("Regeneration saves Ink-Eyes from lethal damage and consumes the shield")
    void regenerationPreventsLethalDamageDestruction() {
        Permanent inkEyes = addCreatureReady(player1, new InkEyesServantOfOni());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new TorrentOfStone()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castAndResolveInstant(player2, 0, inkEyes.getId());

        harness.assertOnBattlefield(player1, "Ink-Eyes, Servant of Oni");
        harness.assertNotInGraveyard(player1, "Ink-Eyes, Servant of Oni");
        assertThat(inkEyes.isTapped()).isTrue();
        assertThat(inkEyes.getMarkedDamage()).isZero();
        assertThat(inkEyes.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Ninjutsu cannot return a blocked attacker")
    void ninjutsuRejectsBlockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new GnarledMass());
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.setHand(player1, List.of(new InkEyesServantOfOni()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Ink-Eyes, Servant of Oni");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    private void attackWithInkEyesDealingDamage() {
        addCreatureReady(player1, new InkEyesServantOfOni()).setAttacking(true);
        resolveCombat();
    }
}
