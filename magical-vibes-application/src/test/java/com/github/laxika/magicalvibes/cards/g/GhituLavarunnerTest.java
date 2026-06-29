package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GhituLavarunnerTest extends BaseCardTest {

    // ===== Below threshold (fewer than 2 instants/sorceries in graveyard) =====

    @Test
    @DisplayName("Base 1/2 without haste when graveyard is empty")
    void noBoostWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Base 1/2 without haste with only one instant in graveyard")
    void noBoostWithOneInstant() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Base 1/2 without haste with only one sorcery in graveyard")
    void noBoostWithOneSorcery() {
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    // ===== At or above threshold (2+ instants/sorceries in graveyard) =====

    @Test
    @DisplayName("Gets +1/+0 and haste with two instants in graveyard")
    void boostWithTwoInstants() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and haste with one instant and one sorcery in graveyard")
    void boostWithOneInstantOneSorcery() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and haste with more than two instants/sorceries in graveyard")
    void boostWithThreeSpells() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Divination()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    // ===== Creature cards in graveyard don't count =====

    @Test
    @DisplayName("Creature cards in graveyard do not count toward threshold")
    void creatureCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(new GhituLavarunner(), new GhituLavarunner()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    // ===== Loses boost when graveyard changes =====

    @Test
    @DisplayName("Loses boost when instants/sorceries are removed from graveyard")
    void losesBoostWhenGraveyardShrinks() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();

        // Remove cards from graveyard — drop below threshold
        harness.setGraveyard(player1, List.of(new Shock()));
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    // ===== Opponent's graveyard doesn't matter =====

    @Test
    @DisplayName("Opponent's graveyard does not affect the boost")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        harness.addToBattlefield(player1, new GhituLavarunner());

        Permanent lavarunner = findLavarunner();
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent findLavarunner() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ghitu Lavarunner"))
                .findFirst().orElseThrow();
    }
}
