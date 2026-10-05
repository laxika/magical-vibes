package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BlindWithAnger;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunkyoBell.class, WanderingOnes.class, JukaiMessenger.class, BlindWithAnger.class})
class JunkyoBellTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting boosts the target by the number of creatures controlled")
    void acceptBoostsByCreatureCount() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());
        addCreatureReady(player1, new JukaiMessenger());

        acceptTargeting(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boosted creature is sacrificed at the beginning of the next end step")
    void boostedCreatureIsSacrificedAtEndStep() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());

        acceptTargeting(target);
        passToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Wandering Ones"));
    }

    @Test
    @DisplayName("Declining leaves the creature unboosted and alive")
    void declineDoesNothing() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getPowerModifier()).isZero();

        passToEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("A creature an opponent controls is not a legal target")
    void opponentCreatureIsNotATarget() {
        addBell(player1);
        Permanent mine = addCreatureReady(player1, new WanderingOnes());
        Permanent theirs = addCreatureReady(player2, new JukaiMessenger());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(mine.getId())
                .doesNotContain(theirs.getId(), findPermanent(player1, "Junkyo Bell").getId());

        harness.handlePermanentChosen(player1, mine.getId());
    }

    @Test
    @DisplayName("Does not sacrifice the target after it changes controller")
    void doesNotSacrificeTargetAfterControlChanges() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());

        acceptTargeting(target);

        harness.setHand(player2, List.of(new BlindWithAnger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));

        passToEndStep();

        assertThat(gqs.findPermanentById(gd, target.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Wandering Ones"));
    }

    @Test
    @DisplayName("Creature count is determined on resolution and the boost then stays fixed")
    void creatureCountIsDeterminedOnResolution() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());
        addCreatureReady(player2, new JukaiMessenger());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        addCreatureReady(player1, new JukaiMessenger());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);

        addCreatureReady(player1, new WanderingOnes());

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Bell does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("An upkeep without a legal creature target requires no choice")
    void noCreatureRequiresNoChoice() {
        addBell(player1);
        addCreatureReady(player2, new WanderingOnes());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Control is checked when the delayed sacrifice resolves")
    void controlChangesInResponseToDelayedSacrifice() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());
        acceptTargeting(target);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.findPermanentById(gd, target.getId())).isNotNull();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new BlindWithAnger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Wandering Ones"));
    }

    @Test
    @DisplayName("A target stolen before the upkeep ability resolves receives no boost or sacrifice")
    void targetBecomesIllegalBeforeResolution() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new BlindWithAnger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();

        passToEndStep();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("A surviving creature loses the Bell's boost at cleanup")
    void boostExpiresAtCleanup() {
        addBell(player1);
        Permanent target = addCreatureReady(player1, new WanderingOnes());
        acceptTargeting(target);

        harness.setHand(player2, List.of(new BlindWithAnger()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        passToEndStep();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    private void addBell(Player player) {
        harness.addToBattlefield(player, new JunkyoBell());
    }

    private void acceptTargeting(Permanent target) {
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void passToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
