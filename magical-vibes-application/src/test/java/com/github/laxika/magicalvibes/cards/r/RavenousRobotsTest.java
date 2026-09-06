package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousRobots.class, GrizzlyBears.class, Spellbook.class})
class RavenousRobotsTest extends BaseCardTest {

    @Test
    void castingArtifactCreatesRobotToken() {
        harness.addToBattlefield(player1, new RavenousRobots());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 0);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Robot")
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1
                        && permanent.getCard().hasType(CardType.ARTIFACT));
    }

    @Test
    void castingNonArtifactDoesNotCreateRobotToken() {
        harness.addToBattlefield(player1, new RavenousRobots());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Robot"));
    }

    @Test
    void activationGrantsHasteOnlyToControlledCreatureTokensUntilEndOfTurn() {
        Permanent robots = harness.addToBattlefieldAndReturn(player1, new RavenousRobots());
        robots.setSummoningSick(false);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        Permanent token = addRobotToken();
        Permanent opponentToken = addOpponentRobotToken();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(robots), null, null);
        harness.passBothPriorities();

        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(creature.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(opponentToken.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token.hasKeyword(Keyword.HASTE)).isFalse();
    }

    private Permanent addRobotToken() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(player1.getId()).add(token);
        return token;
    }

    private Permanent addOpponentRobotToken() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(player2.getId()).add(token);
        return token;
    }
}
