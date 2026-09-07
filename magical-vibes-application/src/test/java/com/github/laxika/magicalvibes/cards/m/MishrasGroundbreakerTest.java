package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SchoolOfTheUnseen;
import com.github.laxika.magicalvibes.cards.s.SolGrail;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MishrasGroundbreaker.class, SchoolOfTheUnseen.class, SolGrail.class})
class MishrasGroundbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes a 3/3 creature")
    void targetLandBecomesThreeThree() {
        addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent land = addCreatureReady(player1, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    @DisplayName("Animated land is an artifact and is still a land")
    void animatedLandIsArtifactAndStillLand() {
        addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent land = addCreatureReady(player1, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.isArtifact(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Animation and artifact type survive end-of-turn cleanup")
    void animationSurvivesCleanup() {
        addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent land = addCreatureReady(player1, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        land.resetModifiers();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.isArtifact(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Activating sacrifices Mishra's Groundbreaker as a cost")
    void activatingSacrificesSelf() {
        Permanent groundbreaker = addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent land = addCreatureReady(player1, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 0, null, land.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(groundbreaker);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == groundbreaker.getCard());
    }

    @Test
    @DisplayName("Can animate an opponent's land")
    void canAnimateOpponentLand() {
        addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent opponentLand = addCreatureReady(player2, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 0, null, opponentLand.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, opponentLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentLand)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent nonland = addCreatureReady(player1, new SolGrail());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonland.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate a tapped Groundbreaker")
    void cannotActivateWhileTapped() {
        Permanent groundbreaker = addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent land = addCreatureReady(player1, new SchoolOfTheUnseen());
        groundbreaker.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target land leaves before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addCreatureReady(player1, new MishrasGroundbreaker());
        Permanent land = addCreatureReady(player1, new SchoolOfTheUnseen());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

}
