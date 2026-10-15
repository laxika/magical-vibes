package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PanicSpellbomb.class, GrizzlyBears.class, CopperMyr.class, Shatter.class})
class PanicSpellbombTest extends BaseCardTest {


    @Test
    @DisplayName("Activating ability sacrifices spellbomb and prompts death trigger")
    void activateAbilitySacrificesAndPromptsMayAbility() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Spellbomb should be sacrificed
        harness.assertNotOnBattlefield(player1, "Panic Spellbomb");
        harness.assertInGraveyard(player1, "Panic Spellbomb");

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Death trigger may ability should prompt
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Target creature can't block after ability resolves")
    void targetCreatureCantBlock() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the can't-block ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Decline the death trigger draw
        harness.handleMayAbilityChosen(player1, false);

        // Resolve the can't-block ability
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }


    @Test
    @DisplayName("Accepting death trigger and paying {R} draws a card")
    void acceptDeathTriggerDrawsCard() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Accept death trigger — pay {R} (draw resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Red mana should be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);

        // Resolve the can't-block ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining death trigger does not draw a card")
    void declineDeathTriggerNoCard() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Decline death trigger
        harness.handleMayAbilityChosen(player1, false);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Red mana should not be spent
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        // Resolve the can't-block ability
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting death trigger without enough mana treats as decline")
    void acceptWithoutManaNoCard() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Accept but cannot pay {R}
        harness.handleMayAbilityChosen(player1, true);

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);

        // Resolve the can't-block ability
        harness.passBothPriorities();
    }


    @Test
    @DisplayName("Both abilities work: creature can't block AND controller draws a card")
    void bothAbilitiesWork() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.addMana(player1, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, bearsId);

        // Resolve the death trigger above the activated ability
        harness.passBothPriorities();

        // Accept death trigger — pay {R} to draw (resolves inline)
        harness.handleMayAbilityChosen(player1, true);

        // Card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);

        // Resolve the can't-block ability
        harness.passBothPriorities();

        // Creature can't block
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Tapped spellbomb cannot pay its activation cost")
    void tappedSpellbombCannotActivate() {
        Permanent spellbomb = harness.addToBattlefieldAndReturn(player1, new PanicSpellbomb());
        spellbomb.tap();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CopperMyr()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Panic Spellbomb");
        harness.assertNotInGraveyard(player1, "Panic Spellbomb");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Noncreature artifact is not a legal target")
    void noncreatureTargetIsRejectedBeforeSacrifice() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new PanicSpellbomb()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Panic Spellbomb");
        harness.assertNotInGraveyard(player1, "Panic Spellbomb");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Destruction without activation still lets the controller pay and draw")
    void destructionTriggersDrawWithoutCreatureTarget() {
        UUID spellbombId = harness.addToBattlefieldAndReturn(player1, new PanicSpellbomb()).getId();
        harness.setLibrary(player1, List.of(new CopperMyr()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, spellbombId);
        harness.assertInGraveyard(player1, "Panic Spellbomb");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Copper Myr");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Draw trigger remains independent when the creature target leaves")
    void targetLeavingDoesNotPreventDraw() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CopperMyr()).getId();
        harness.setLibrary(player1, List.of(new PanicSpellbomb()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, targetId);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.assertInGraveyard(player2, "Copper Myr");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Panic Spellbomb");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Generic mana cannot pay the red draw cost")
    void colorlessManaCannotPayDrawCost() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CopperMyr()).getId();
        harness.setLibrary(player1, List.of(new PanicSpellbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Copper Myr").isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Blocking restriction affects only its target and expires this turn")
    void restrictionExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new PanicSpellbomb());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new CopperMyr());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(other.isCantBlockThisTurn()).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.UPKEEP);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }

}
