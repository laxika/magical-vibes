package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StorySeeker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MajaBretagardProtector.class, Forest.class, StorySeeker.class})
class MajaBretagardProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void boostsOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new MajaBretagardProtector());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new StorySeeker());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new StorySeeker());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Landfall creates a 1/1 white Human Warrior token")
    void landfallCreatesHumanWarriorToken() {
        harness.addToBattlefield(player1, new MajaBretagardProtector());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Human Warrior"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.WARRIOR);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new MajaBretagardProtector());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Human Warrior")))
                .isEmpty();
    }

    @Test
    @DisplayName("Maja does not boost herself and her boost ends when she leaves")
    void excludesSelfAndBoostEndsWhenMajaLeaves() {
        Permanent maja = harness.addToBattlefieldAndReturn(player1, new MajaBretagardProtector());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StorySeeker());

        assertThat(gqs.getEffectivePower(gd, maja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, maja)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, maja));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A pending landfall trigger creates its token even after Maja leaves")
    void pendingLandfallResolvesAfterMajaLeaves() {
        Permanent maja = harness.addToBattlefieldAndReturn(player1, new MajaBretagardProtector());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, maja));
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Each land entering without being played triggers landfall")
    void landfallTriggersForEachLandPutOntoBattlefield() {
        harness.addToBattlefield(player1, new MajaBretagardProtector());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        });
    }
}
