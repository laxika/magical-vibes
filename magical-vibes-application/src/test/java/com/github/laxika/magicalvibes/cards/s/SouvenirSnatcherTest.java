package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.p.PhyrexianHulk;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SouvenirSnatcher.class, FountainOfYouth.class, PhyrexianHulk.class})
class SouvenirSnatcherTest extends BaseCardTest {

    @Test
    void mutatingGainsControlOfTargetNoncreatureArtifact() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void mutatingCannotTargetArtifactCreature() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent hulk = harness.addToBattlefieldAndReturn(player2, new PhyrexianHulk());
        harness.addToBattlefield(player2, new FountainOfYouth());

        triggerMutation(snatcher);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hulk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void castingNormallyDoesNotStealAnArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.castFromHand(player1, new SouvenirSnatcher(), "{4}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Souvenir Snatcher");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controlTriggerResolvesAfterSnatcherDies() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, fountain.getId());
        snatcher.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Souvenir Snatcher");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void controlDoesNotExpireWhenSnatcherDies() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();
        snatcher.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Souvenir Snatcher");
        harness.assertOnBattlefield(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void repeatedMutationsStealDifferentArtifactsWithoutReturningTheFirst() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(first, second);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(first, second);
    }

    @Test
    void canTargetAnArtifactAlreadyControlledByTheTriggerController() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        triggerMutation(snatcher);
        harness.handlePermanentChosen(player1, fountain.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fountain);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mutationWithOnlyArtifactCreaturesAvailableDoesNotStealAnything() {
        Permanent snatcher = addCreatureReady(player1, new SouvenirSnatcher());
        harness.addToBattlefield(player2, new PhyrexianHulk());

        triggerMutation(snatcher);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertOnBattlefield(player2, "Phyrexian Hulk");
        harness.assertNotOnBattlefield(player1, "Phyrexian Hulk");
    }

    private void triggerMutation(Permanent snatcher) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, snatcher, List.of(snatcher.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
