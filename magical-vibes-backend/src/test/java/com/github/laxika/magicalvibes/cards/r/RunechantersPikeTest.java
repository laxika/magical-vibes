package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RunechantersPikeTest extends BaseCardTest {

    // ===== First strike =====

    @Test
    @DisplayName("Equipped creature gains first strike")
    void equippedCreatureGainsFirstStrike() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creature does not gain first strike from Pike")
    void unequippedCreatureNoFirstStrike() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    // ===== Power boost =====

    @Test
    @DisplayName("No boost with empty graveyard")
    void noBoostWithEmptyGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts instant cards in controller's graveyard")
    void boostCountsInstants() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        // 2 base + 2 instants = 4 power, toughness unchanged at 2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts sorcery cards in controller's graveyard")
    void boostCountsSorceries() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        gd.playerGraveyards.get(player1.getId()).add(new Divination());

        // 2 base + 1 sorcery = 3 power, toughness unchanged at 2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost counts both instants and sorceries")
    void boostCountsBothInstantsAndSorceries() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new Divination());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        // 2 base + 2 instants + 1 sorcery = 5 power, toughness unchanged at 2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature cards in graveyard do not count")
    void creatureCardsDoNotCount() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        // No boost from creature cards
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Only controller's graveyard =====

    @Test
    @DisplayName("Does not count opponent's graveyard")
    void doesNotCountOpponentGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        // Only opponent has instants/sorceries in graveyard
        gd.playerGraveyards.get(player2.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new Divination());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts equipment controller's graveyard, not equipped creature's controller's")
    void countsEquipmentControllersGraveyard() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        // Pike controlled by player1, attached to player2's creature
        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(opponentBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new Shock());
        gd.playerGraveyards.get(player2.getId()).add(new Shock());

        // Should count player1's 2 instants, not player2's 3
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4); // 2 base + 2 instants
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    // ===== Dynamic updates =====

    @Test
    @DisplayName("Boost updates dynamically as graveyard changes")
    void boostUpdatesDynamically() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).add(new Divination());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        // Remove all cards from graveyard
        gd.playerGraveyards.get(player1.getId()).clear();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    // ===== Equip transfers =====

    @Test
    @DisplayName("Equipping to another creature transfers boost and first strike")
    void equipTransfersBoostandFirstStrike() {
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2);

        Permanent pike = new Permanent(new RunechantersPike());
        pike.setAttachedTo(bears1.getId());
        gd.playerBattlefields.get(player1.getId()).add(pike);

        gd.playerGraveyards.get(player1.getId()).add(new Shock());

        // bears1 has boost and first strike
        assertThat(gqs.getEffectivePower(gd, bears1)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears1, Keyword.FIRST_STRIKE)).isTrue();

        // Move pike to bears2
        pike.setAttachedTo(bears2.getId());

        // bears1 loses boost and first strike
        assertThat(gqs.getEffectivePower(gd, bears1)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears1, Keyword.FIRST_STRIKE)).isFalse();

        // bears2 gains boost and first strike
        assertThat(gqs.getEffectivePower(gd, bears2)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears2, Keyword.FIRST_STRIKE)).isTrue();
    }
}
