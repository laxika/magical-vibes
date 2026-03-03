package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaguemawBeastTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has tap + sacrifice creature cost + proliferate activated ability")
    void hasCorrectAbilityStructure() {
        PlaguemawBeast card = new PlaguemawBeast();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(ProliferateEffect.class);
    }

    // ===== Sacrifice + Proliferate =====

    @Test
    @DisplayName("Sacrifices chosen creature and proliferates -1/-1 counters")
    void sacrificesCreatureAndProliferatesMinusCounters() {
        addReadyBeast(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        Permanent enemyBears = new Permanent(new GrizzlyBears());
        enemyBears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(enemyBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, elvesId); // sacrifice Llanowar Elves
        harness.passBothPriorities(); // resolve ability

        // Choose enemy bears for proliferate
        harness.handleMultiplePermanentsChosen(player1, List.of(enemyBears.getId()));

        // Llanowar Elves should be sacrificed
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");

        // Bears should have 2 -1/-1 counters
        assertThat(enemyBears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices chosen creature and proliferates +1/+1 counters")
    void sacrificesCreatureAndProliferatesPlusCounters() {
        addReadyBeast(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        Permanent allyBears = new Permanent(new GrizzlyBears());
        allyBears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(allyBears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(allyBears.getId()));

        assertThat(allyBears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can sacrifice itself to activate ability")
    void canSacrificeItself() {
        Permanent beast = addReadyBeast(player1);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, beast.getId()); // sacrifice itself
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player1, "Plaguemaw Beast");
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent beast = addReadyBeast(player1);
        beast.tap();
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elvesId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        PlaguemawBeast card = new PlaguemawBeast();
        Permanent beast = new Permanent(card);
        // summoning sick by default
        gd.playerBattlefields.get(player1.getId()).add(beast);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, elvesId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Proliferate can choose no permanents")
    void proliferateCanChooseNone() {
        addReadyBeast(player1);
        harness.addToBattlefield(player1, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        // Choose nothing
        harness.handleMultiplePermanentsChosen(player1, List.of());

        // Counter unchanged
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreatureToSacrifice() {
        addReadyBeast(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyBeast(Player player) {
        PlaguemawBeast card = new PlaguemawBeast();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
