package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BruseTarlBoorishHerder.class, GrizzlyBears.class})
class BruseTarlBoorishHerderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and grants target creature double strike and lifelink")
    void entersAndGrantsKeywords() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.castFromHand(player1, new BruseTarlBoorishHerder(), "{2}{R}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger grants both keywords only to a creature you control")
    void attackGrantsKeywordsToOwnCreature() {
        addCreatureReady(player1, new BruseTarlBoorishHerder());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingTarget = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownTarget.getId()).doesNotContain(opposingTarget.getId());

        harness.handlePermanentChosen(player1, ownTarget.getId());
        harness.passBothPriorities();

        assertThat(ownTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(ownTarget.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(opposingTarget.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(opposingTarget.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void grantedKeywordsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new BruseTarlBoorishHerder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Bruse can target himself with his entry trigger")
    void entryTriggerCanTargetBruseHimself() {
        harness.castFromHand(player1, new BruseTarlBoorishHerder(), "{2}{R}{W}");
        harness.passBothPriorities();

        Permanent bruse = findPermanent(player1, "Bruse Tarl, Boorish Herder");
        harness.handlePermanentChosen(player1, bruse.getId());
        harness.passBothPriorities();

        assertThat(bruse.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(bruse.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Attack trigger does not grant keywords to a target that leaves the battlefield")
    void removedTargetDoesNotGainKeywords() {
        Permanent bruse = addCreatureReady(player1, new BruseTarlBoorishHerder());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
        assertThat(bruse.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(bruse.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
