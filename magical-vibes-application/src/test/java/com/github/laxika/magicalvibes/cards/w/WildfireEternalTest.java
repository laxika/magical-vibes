package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LifeGoesOn;
import com.github.laxika.magicalvibes.cards.o.OverwhelmingSplendor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildfireEternal.class, Divination.class, GrizzlyBears.class, LifeGoesOn.class, OverwhelmingSplendor.class})
class WildfireEternalTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = harness.addToBattlefieldAndReturn(player1, new WildfireEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        return atk;
    }

    private void addBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
    }

    @Test
    @DisplayName("Unblocked: may cast an instant/sorcery from hand for free")
    void unblockedMayCastSpellForFree() {
        Divination spell = new Divination();
        harness.setHand(player1, new ArrayList<>(List.of(spell)));
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Unblocked: creature in hand is not offered")
    void unblockedDoesNotOfferCreature() {
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining leaves the spell in hand")
    void decliningCastsNothing() {
        Divination spell = new Divination();
        harness.setHand(player1, new ArrayList<>(List.of(spell)));
        addAttacker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Afflict 4: becoming blocked makes the defending player lose 4 life")
    void blockedAfflictsDefender() {
        harness.setHand(player1, new ArrayList<>(List.of(new Divination())));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker();
        addBlocker();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Afflict fires; free-cast does not (creature was blocked).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked: an instant can be cast without mana before combat damage")
    void unblockedCastsInstantBeforeDamage() {
        LifeGoesOn spell = new LifeGoesOn();
        harness.setHand(player1, List.of(spell));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addAttacker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(spell.getId());
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Life Goes On");
    }

    @Test
    @DisplayName("Only one eligible spell can be cast per unblocked trigger")
    void castsOnlyOneSpellPerTrigger() {
        harness.setHand(player1, List.of(new LifeGoesOn(), new LifeGoesOn()));
        addAttacker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getId())
                .isNotEqualTo(gd.stack.getFirst().getCard().getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Afflict triggers once even with two blockers")
    void multipleBlockersAfflictOnlyOnce() {
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        addAttacker();
        harness.addToBattlefield(player2, new WildfireEternal());
        harness.addToBattlefield(player2, new WildfireEternal());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Losing all abilities prevents the unblocked free-cast trigger")
    void losingAbilitiesSuppressesFreeCastTrigger() {
        harness.setHand(player1, List.of(new LifeGoesOn()));
        addAttacker();
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new OverwhelmingSplendor());
        curse.setAttachedTo(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Life Goes On");
    }

    @Test
    @DisplayName("Losing all abilities prevents afflict")
    void losingAbilitiesSuppressesAfflict() {
        harness.setHand(player1, List.of());
        harness.setLife(player2, 20);
        addAttacker();
        harness.addToBattlefield(player2, new WildfireEternal());
        Permanent curse = harness.addToBattlefieldAndReturn(player2, new OverwhelmingSplendor());
        curse.setAttachedTo(player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }
}
