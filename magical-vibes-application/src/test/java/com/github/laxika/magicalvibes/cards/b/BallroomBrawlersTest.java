package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BallroomBrawlers.class, BackupAgent.class})
class BallroomBrawlersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can grant first strike to this creature and another creature you control")
    void grantsFirstStrikeToBothCreatures() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent bear = addCreatureReady(player1, new BackupAgent());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "First strike");

        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Attacking can grant lifelink to this creature and another creature you control")
    void grantsLifelinkToBothCreatures() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent bear = addCreatureReady(player1, new BackupAgent());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The granted keyword wears off at end of turn")
    void grantedKeywordWearsOffAtEndOfTurn() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent bear = addCreatureReady(player1, new BackupAgent());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "First strike");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The optional target may be declined")
    void mayDeclineTarget() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The target must be another creature you control")
    void targetMustBeAnotherCreatureYouControl() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent opponentBear = addCreatureReady(player2, new BackupAgent());
        Permanent ownBear = addCreatureReady(player1, new BackupAgent());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, brawlers.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownBear.getId());
    }

    @Test
    @DisplayName("Choosing no target remains final when another creature is available")
    void decliningAvailableTargetDoesNotPromptAgainDuringResolution() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent other = addCreatureReady(player1, new BallroomBrawlers());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "First strike");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("An illegal sole target prevents the entire ability from resolving")
    void removedTargetPreventsSelfGrant() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent other = addCreatureReady(player1, new BallroomBrawlers());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, other.getId());
        gd.playerBattlefields.get(player1.getId()).remove(other);
        gd.playerGraveyards.get(player1.getId()).add(other.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The target still gains the chosen keyword if Ballroom Brawlers leaves")
    void removedSourceDoesNotPreventTargetGrant() {
        Permanent brawlers = addCreatureReady(player1, new BallroomBrawlers());
        Permanent other = addCreatureReady(player1, new BallroomBrawlers());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, other.getId());
        gd.playerBattlefields.get(player1.getId()).remove(brawlers);
        gd.playerGraveyards.get(player1.getId()).add(brawlers.getCard());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, other, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }
}