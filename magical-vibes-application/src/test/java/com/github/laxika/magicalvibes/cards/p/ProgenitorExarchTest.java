package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EsixFractalBloom;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProgenitorExarch.class, EsixFractalBloom.class})
class ProgenitorExarchTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X Incubator tokens, each with three +1/+1 counters")
    void entersWithXIncubatorTokens() {
        castProgenitorExarch(2);
        resolveProgenitorExarch();

        List<Permanent> incubators = findPermanents(player1, "Incubator");
        assertThat(incubators).hasSize(2);
        assertThat(incubators)
                .allSatisfy(incubator -> assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(3));
    }

    @Test
    @DisplayName("Transforms a targeted Incubator token you control")
    void transformsTargetedIncubator() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();

        Permanent exarch = findPermanent(player1, "Progenitor Exarch");
        exarch.setSummoningSick(false);
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(exarch), null,
                incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Rejects a target that is not an Incubator token you control")
    void rejectsNonIncubatorTarget() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();

        Permanent exarch = findPermanent(player1, "Progenitor Exarch");
        exarch.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(exarch), null, exarch.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Incubator token you control");
    }

    @Test
    @DisplayName("X zero creates no Incubators")
    void zeroXCreatesNoTokens() {
        castProgenitorExarch(0);
        resolveProgenitorExarch();

        assertThat(findPermanents(player1, "Incubator")).isEmpty();
        harness.assertOnBattlefield(player1, "Progenitor Exarch");
    }

    @Test
    @DisplayName("An Incubator can pay two mana to transform and retains its counters")
    void incubatorTransformsUsingItsOwnAbility() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A transformed Incubator is no longer a legal target for the Exarch")
    void rejectsTransformedToken() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();
        Permanent exarch = findPermanent(player1, "Progenitor Exarch");
        exarch.setSummoningSick(false);
        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(exarch), null, incubator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Incubator token you control");
    }

    @Test
    @DisplayName("The Exarch cannot transform an opponent's Incubator")
    void rejectsOpponentsIncubator() {
        castProgenitorExarch(1);
        resolveProgenitorExarch();
        Permanent exarch = findPermanent(player1, "Progenitor Exarch");
        exarch.setSummoningSick(false);
        Permanent incubator = findPermanent(player1, "Incubator");
        gd.playerBattlefields.get(player1.getId()).remove(incubator);
        gd.playerBattlefields.get(player2.getId()).add(incubator);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(exarch), null, incubator.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Incubator token you control");
    }

    @Test
    @CardUsed({ProgenitorExarch.class, EsixFractalBloom.class})
    @DisplayName("Esix replaces only the first of multiple separate incubate actions")
    void incubateActionsCreateTokensSeparately() {
        harness.addToBattlefield(player1, new EsixFractalBloom());
        castProgenitorExarch(2);
        resolveProgenitorExarch();
        Permanent exarch = findPermanent(player1, "Progenitor Exarch");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, exarch.getId());

        assertThat(findPermanents(player1, "Progenitor Exarch"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(findPermanents(player1, "Incubator")).hasSize(1);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
        harness.passBothPriorities();
    }

    private void castProgenitorExarch(int xValue) {
        harness.setHand(player1, List.of(new ProgenitorExarch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue * 2);
        harness.castCreature(player1, 0, xValue);
    }

    private void resolveProgenitorExarch() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
