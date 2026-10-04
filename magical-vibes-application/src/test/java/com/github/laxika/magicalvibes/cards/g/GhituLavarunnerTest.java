package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhituLavarunner.class, Shock.class, Divination.class})
class GhituLavarunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Base 1/2 without haste when graveyard is empty")
    void noBoostWithEmptyGraveyard() {
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Base 1/2 without haste with only one instant in graveyard")
    void noBoostWithOneInstant() {
        harness.setGraveyard(player1, List.of(new Shock()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Base 1/2 without haste with only one sorcery in graveyard")
    void noBoostWithOneSorcery() {
        harness.setGraveyard(player1, List.of(new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Gets +1/+0 and haste with two instants in graveyard")
    void boostWithTwoInstants() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and haste with one instant and one sorcery in graveyard")
    void boostWithOneInstantOneSorcery() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Gets +1/+0 and haste with more than two instants/sorceries in graveyard")
    void boostWithThreeSpells() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock(), new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creature cards in graveyard do not count toward threshold")
    void creatureCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(new GhituLavarunner(), new GhituLavarunner()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Loses boost when instants/sorceries are removed from graveyard")
    void losesBoostWhenGraveyardShrinks() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();

        // Remove cards from graveyard — drop below threshold
        harness.setGraveyard(player1, List.of(new Shock()));
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's graveyard does not affect the boost")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();
    }


    @Test
    @DisplayName("Two sorceries grant the bonus and haste")
    void boostWithTwoSorceries() {
        harness.setGraveyard(player1, List.of(new Divination(), new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Reaching the threshold grants the bonus immediately")
    void gainsBoostWhenGraveyardGrows() {
        harness.setGraveyard(player1, List.of(new Divination(), new GhituLavarunner()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isFalse();

        harness.setGraveyard(player1, List.of(new Divination(), new Divination(), new GhituLavarunner()));

        assertThat(gqs.getEffectivePower(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lavarunner)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lavarunner, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste allows a newly entered Lavarunner to attack")
    void attacksImmediatelyAtThreshold() {
        harness.setGraveyard(player1, List.of(new Divination(), new Divination()));
        Permanent lavarunner = harness.addToBattlefieldAndReturn(player1, new GhituLavarunner());
        lavarunner.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(lavarunner.isAttacking()).isTrue();
    }
}
