package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DouseInGloom;
import com.github.laxika.magicalvibes.cards.p.PlaguedRusalka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantSolifuge.class, Gristleback.class, DouseInGloom.class, PlaguedRusalka.class})
class GiantSolifugeTest extends BaseCardTest {

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);
        Permanent solifuge = harness.addToBattlefieldAndReturn(player1, new GiantSolifuge());
        solifuge.setSummoningSick(true);

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamage() {
        harness.setLife(player2, 20);

        Permanent solifuge = addCreatureReady(player1, new GiantSolifuge());
        solifuge.setAttacking(true);

        Permanent bears = addCreatureReady(player2, new Gristleback());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                bears.getId(), 2,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Giant Solifuge");
        harness.assertInGraveyard(player2, "Gristleback");
    }

    @Test
    @DisplayName("Cannot be targeted by spells because it has shroud")
    void cannotBeTargetedBySpells() {
        Permanent solifuge = harness.addToBattlefieldAndReturn(player1, new GiantSolifuge());

        harness.setHand(player1, List.of(new DouseInGloom()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, solifuge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Cannot be targeted by abilities because it has shroud")
    void cannotBeTargetedByAbilities() {
        Permanent solifuge = addCreatureReady(player1, new GiantSolifuge());
        addCreatureReady(player2, new PlaguedRusalka());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, solifuge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud also prevents an opponent's spell from targeting it")
    void cannotBeTargetedByOpponentsSpell() {
        Permanent solifuge = harness.addToBattlefieldAndReturn(player1, new GiantSolifuge());
        harness.setHand(player2, List.of(new DouseInGloom()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, solifuge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud also prevents its controller's ability from targeting it")
    void cannotBeTargetedByControllersAbility() {
        Permanent solifuge = addCreatureReady(player1, new GiantSolifuge());
        addCreatureReady(player1, new PlaguedRusalka());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, solifuge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @ParameterizedTest
    @CsvSource({"2, 0", "0, 2", "1, 1"})
    @DisplayName("Hybrid symbols can be paid with red, green, or a mixture")
    void canCastWithEitherHybridColor(int redMana, int greenMana) {
        harness.setHand(player1, List.of(new GiantSolifuge()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, redMana);
        harness.addMana(player1, ManaColor.GREEN, greenMana);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Giant Solifuge");
        harness.assertNotInHand(player1, "Giant Solifuge");
    }
}
