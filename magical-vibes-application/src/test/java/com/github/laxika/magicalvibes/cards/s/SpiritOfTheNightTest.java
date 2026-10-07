package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritOfTheNight.class, BayFalcon.class, FeralShadow.class})
class SpiritOfTheNightTest extends BaseCardTest {

    private Permanent addSpirit() {
        Permanent spirit = addCreatureReady(player1, new SpiritOfTheNight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return spirit;
    }

    @Test
    @DisplayName("Has protection from black but not from other colors")
    void hasProtectionFromBlack() {
        Permanent spirit = addSpirit();

        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, spirit, CardColor.GREEN)).isFalse();
    }

    @Test
    @DisplayName("Has first strike only while attacking")
    void firstStrikeOnlyWhileAttacking() {
        Permanent spirit = addSpirit();

        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();

        spirit.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isTrue();

        spirit.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Haste allows attacking on the turn Spirit enters")
    void attacksWhileSummoningSick() {
        harness.addToBattlefield(player1, new SpiritOfTheNight());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Attacking Spirit deals first-strike trample damage before its blocker")
    void attackingFirstStrikeAndTrample() {
        Permanent spirit = addSpirit();
        Permanent blocker = addCreatureReady(player2, new BayFalcon());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5));

        harness.assertInGraveyard(player2, "Bay Falcon");
        harness.assertOnBattlefield(player1, "Spirit of the Night");
        harness.assertLife(player2, 15);
        assertThat(spirit.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A blocking Spirit does not have first strike")
    void blockingSpiritTakesNormalCombatDamage() {
        Permanent spirit = addSpirit();
        addCreatureReady(player2, new BayFalcon());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        assertThat(gqs.hasKeyword(gd, spirit, Keyword.FIRST_STRIKE)).isFalse();
        resolveCombat(player2);

        harness.assertInGraveyard(player2, "Bay Falcon");
        harness.assertOnBattlefield(player1, "Spirit of the Night");
        assertThat(spirit.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection prevents a black flying creature from blocking")
    void blackFlyingCreatureCannotBlock() {
        addSpirit();
        addCreatureReady(player2, new FeralShadow());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spirit can block a black attacker and prevents its damage")
    void preventsBlackCombatDamageWhileBlocking() {
        Permanent spirit = addSpirit();
        addCreatureReady(player2, new FeralShadow());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertInGraveyard(player2, "Feral Shadow");
        harness.assertOnBattlefield(player1, "Spirit of the Night");
        harness.assertLife(player1, 20);
        assertThat(spirit.getMarkedDamage()).isZero();
    }
}
