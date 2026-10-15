package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CaptivatingVampire;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrefanMaurerProgenitor.class, CaptivatingVampire.class, SculptingSteel.class})
class StrefanMaurerProgenitorTest extends BaseCardTest {

    @Test
    void createsOneBloodForEachPlayerWhoLostLife() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 2);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Blood")).hasSize(2);
    }

    @Test
    void doesNotCreateBloodWhenNoPlayerLostLife() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void sacrificesTwoBloodToPutVampireOntoBattlefieldTappedAndAttackingWithIndestructible() {
        Permanent strefan = addCreatureReady(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        advanceToEndStep();

        harness.setHand(player1, List.of(new CaptivatingVampire()));
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(strefan)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        Permanent vampire = findPermanent(player1, "Captivating Vampire");
        assertThat(vampire.isTapped()).isTrue();
        assertThat(vampire.isAttacking()).isTrue();
        assertThat(vampire.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void countsLifeLossEvenWhenThePlayerGainedMoreLife() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 2, "life loss");
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 5);
        });

        advanceToEndStep();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void createsOneBloodWhenOnlyOpponentLostLife() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player2.getId(), 7);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void mayDeclineToSacrificeBlood() {
        Permanent strefan = prepareAttackWithBlood(2);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        attackWith(strefan);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Blood")).hasSize(2);
        harness.assertInHand(player1, "Captivating Vampire");
        harness.assertNotOnBattlefield(player1, "Captivating Vampire");
    }

    @Test
    void cannotSacrificeOnlyOneBlood() {
        Permanent strefan = prepareAttackWithBlood(1);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        attackWith(strefan);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        harness.assertInHand(player1, "Captivating Vampire");
        harness.assertNotOnBattlefield(player1, "Captivating Vampire");
    }

    @Test
    void mayDeclineToPutVampireAfterSacrificingBlood() {
        Permanent strefan = prepareAttackWithBlood(2);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        attackWith(strefan);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        harness.assertInHand(player1, "Captivating Vampire");
        harness.assertNotOnBattlefield(player1, "Captivating Vampire");
    }

    @Test
    void canSacrificeBloodWithNoVampireInHand() {
        Permanent strefan = prepareAttackWithBlood(2);
        harness.setHand(player1, List.of(new SculptingSteel()));

        attackWith(strefan);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
        harness.assertInHand(player1, "Sculpting Steel");
        harness.assertNotOnBattlefield(player1, "Sculpting Steel");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesExactlyTwoBloodWhenMoreAreAvailable() {
        Permanent strefan = prepareAttackWithBlood(2);
        advanceToEndStep();
        List<Permanent> blood = findPermanents(player1, "Blood");
        assertThat(blood).hasSize(4);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        attackWith(strefan);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(blood.get(0).getId(), blood.get(2).getId()));
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Blood")).containsExactly(blood.get(1), blood.get(3));
        harness.assertOnBattlefield(player1, "Captivating Vampire");
        harness.assertNotInHand(player1, "Captivating Vampire");
    }

    @Test
    void indestructibleExpiresAtEndOfTurn() {
        Permanent strefan = prepareAttackWithBlood(2);
        harness.setHand(player1, List.of(new CaptivatingVampire()));
        attackWith(strefan);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);
        Permanent vampire = findPermanent(player1, "Captivating Vampire");
        assertThat(gqs.hasKeyword(gd, vampire, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, vampire, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotSacrificeNontokenCopyOfBlood() {
        Permanent strefan = prepareAttackWithBlood(1);
        Permanent blood = findPermanent(player1, "Blood");
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SculptingSteel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, blood.getId());
        assertThat(findPermanents(player1, "Blood")).hasSize(2);
        assertThat(findPermanents(player1, "Blood").stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
        harness.setHand(player1, List.of(new CaptivatingVampire()));

        attackWith(strefan);
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).hasSize(2);
        harness.assertInHand(player1, "Captivating Vampire");
    }

    private Permanent prepareAttackWithBlood(int count) {
        Permanent strefan = addCreatureReady(player1, new StrefanMaurerProgenitor());
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        if (count == 2) {
            gd.lifeLostThisTurn.put(player2.getId(), 1);
        }
        advanceToEndStep();
        return strefan;
    }

    private void attackWith(Permanent strefan) {
        gd.playerAutoStopSteps.put(player1.getId(), EnumSet.of(
                TurnStep.DECLARE_ATTACKERS, TurnStep.DECLARE_BLOCKERS));
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(strefan)));
        resolveAllTriggers();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
