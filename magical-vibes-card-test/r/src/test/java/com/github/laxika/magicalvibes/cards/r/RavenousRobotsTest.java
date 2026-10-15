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
        return harness.addToBattlefieldAndReturn(player1, tokenCard);
    }

    private Permanent addOpponentRobotToken() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return harness.addToBattlefieldAndReturn(player2, tokenCard);
    }

    @Test
    void tokenCopyGrantsHasteToItself() {
        RavenousRobots tokenCard = new RavenousRobots();
        tokenCard.setToken(true);
        Permanent robots = harness.addToBattlefieldAndReturn(player1, tokenCard);
        robots.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(robots.isTapped()).isTrue();
        assertThat(robots.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void castingRobotsDoesNotTriggerItsOwnAbility() {
        harness.setHand(player1, List.of(new RavenousRobots()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ravenous Robots");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void opponentCastingArtifactDoesNotCreateToken() {
        harness.addToBattlefield(player1, new RavenousRobots());
        harness.setHand(player2, List.of(new Spellbook()));
        gd.activePlayerId = player2.getId();

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void artifactCreatureCastCreatesTokenBeforeSpellResolves() {
        harness.addToBattlefield(player1, new RavenousRobots());
        harness.setHand(player1, List.of(new RavenousRobots()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getName()).isEqualTo("Robot");
                    assertThat(permanent.getCard().getPower()).isEqualTo(1);
                    assertThat(permanent.getCard().getToughness()).isEqualTo(1);
                    assertThat(permanent.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(permanent.getCard().hasType(CardType.ARTIFACT)).isTrue();
                    assertThat(permanent.getCard().getColor()).isNull();
                });
    }

    @Test
    void tokensCreatedAfterHasteAbilityResolvesDoNotGainHaste() {
        Permanent robots = harness.addToBattlefieldAndReturn(player1, new RavenousRobots());
        robots.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> assertThat(token.hasKeyword(Keyword.HASTE)).isFalse());
    }
}
