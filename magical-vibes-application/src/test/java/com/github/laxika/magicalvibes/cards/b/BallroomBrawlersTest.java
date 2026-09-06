package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BallroomBrawlers.class, GrizzlyBears.class})
class BallroomBrawlersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking can grant first strike to this creature and another creature you control")
    void grantsFirstStrikeToBothCreatures() {
        Permanent brawlers = addReadyCreature(player1, new BallroomBrawlers());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

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
        Permanent brawlers = addReadyCreature(player1, new BallroomBrawlers());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

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
        Permanent brawlers = addReadyCreature(player1, new BallroomBrawlers());
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

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
        Permanent brawlers = addReadyCreature(player1, new BallroomBrawlers());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lifelink");

        assertThat(gqs.hasKeyword(gd, brawlers, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("The target must be another creature you control")
    void targetMustBeAnotherCreatureYouControl() {
        Permanent brawlers = addReadyCreature(player1, new BallroomBrawlers());
        Permanent opponentBear = addReadyCreature(player2, new GrizzlyBears());
        Permanent ownBear = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, brawlers.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentBear.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownBear.getId());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
