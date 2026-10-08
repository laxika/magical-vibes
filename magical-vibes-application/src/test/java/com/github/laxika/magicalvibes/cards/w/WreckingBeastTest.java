package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({WreckingBeast.class})
class WreckingBeastTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the Riot counter gives Wrecking Beast a +1/+1 counter")
    void riotAddsCounter() {
        Permanent beast = castBeast(true);

        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, beast)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, beast)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Choosing Riot haste gives Wrecking Beast lasting haste")
    void riotAddsPersistentHaste() {
        Permanent beast = castBeast(false);

        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Riot is chosen before Wrecking Beast enters the battlefield")
    void riotChoicePrecedesEntry() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new WreckingBeast(), "{5}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotOnBattlefield(player1, "Wrecking Beast");

        harness.handleMayAbilityChosen(player1, false);

        Permanent beast = findPermanent(player1, "Wrecking Beast");
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, beast, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Choosing haste allows Wrecking Beast to attack on the turn it enters")
    void hasteAllowsImmediateAttack() {
        castBeast(false);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Choosing a counter leaves Wrecking Beast unable to attack immediately")
    void counterDoesNotAllowImmediateAttack() {
        castBeast(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Trample deals excess damage after accounting for the riot counter")
    void trampleWithRiotCounter() {
        Permanent beast = castBeast(true);
        beast.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new WreckingBeast());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 6, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Wrecking Beast");
        harness.assertOnBattlefield(player1, "Wrecking Beast");
    }

    private Permanent castBeast(boolean chooseCounter) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new WreckingBeast(), "{5}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, chooseCounter);

        return findPermanent(player1, "Wrecking Beast");
    }
}
