package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShimmerDragon.class, Spellbook.class, Ornithopter.class})
class ShimmerDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Has hexproof while its controller controls four or more artifacts")
    void hasHexproofWithFourArtifacts() {
        Permanent dragon = addDragon();
        addArtifacts(4);

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Does not have hexproof while its controller controls fewer than four artifacts")
    void lacksHexproofWithFewerThanFourArtifacts() {
        Permanent dragon = addDragon();
        addArtifacts(3);

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Tapping two artifacts draws a card")
    void tapsTwoArtifactsAndDraws() {
        Permanent dragon = addDragon();
        Permanent firstArtifact = addArtifact();
        Permanent secondArtifact = addArtifact();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragon), null, null);
        harness.passBothPriorities();

        assertThat(firstArtifact.isTapped()).isTrue();
        assertThat(secondArtifact.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate without two untapped artifacts")
    void requiresTwoUntappedArtifacts() {
        Permanent dragon = addDragon();
        addArtifact();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(dragon), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    private Permanent addDragon() {
        return addCreatureReady(player1, new ShimmerDragon());
    }

    @Test
    void hexproofTracksArtifactsEnteringAndLeavingAndCountsTappedArtifacts() {
        Permanent dragon = addDragon();
        addArtifacts(3);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();

        Permanent fourth = addArtifact();
        fourth.tap();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fourth);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void opposingArtifactsDoNotGrantHexproofOrPayTheCost() {
        Permanent dragon = addDragon();
        addArtifact();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Spellbook());
        }

        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    void tappedArtifactsCannotPayTheCost() {
        addDragon();
        addArtifact();
        addArtifact().tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents to tap");
    }

    @Test
    void tappedSummoningSickDragonCanTapSummoningSickArtifactCreatures() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new ShimmerDragon());
        dragon.setSummoningSick(true);
        dragon.tap();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new Spellbook()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Spellbook");
    }

    private Permanent addArtifact() {
        return harness.addToBattlefieldAndReturn(player1, new Spellbook());
    }

    private void addArtifacts(int count) {
        for (int i = 0; i < count; i++) {
            addArtifact();
        }
    }
}
