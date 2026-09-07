package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RysorianBadger;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeldraneOfSengir.class, RysorianBadger.class, Forest.class})
class VeldraneOfSengirTest extends BaseCardTest {

    private Permanent addVeldrane() {
        return addCreatureReady(player1, new VeldraneOfSengir());
    }

    @Test
    @DisplayName("Ability gives -3/-0 until end of turn")
    void abilityAppliesNegativeBoost() {
        Permanent veldrane = addVeldrane();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, veldrane, Keyword.FORESTWALK)).isTrue();
        assertThat(veldrane.getPowerModifier()).isEqualTo(-3);
        assertThat(veldrane.getToughnessModifier()).isEqualTo(0);
        assertThat(veldrane.getEffectivePower()).isEqualTo(2);
        assertThat(veldrane.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("The ability can be activated multiple times in one turn")
    void abilityCanBeActivatedMultipleTimes() {
        Permanent veldrane = addVeldrane();
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(veldrane.getEffectivePower()).isEqualTo(-1);
        assertThat(veldrane.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, veldrane, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted forestwalk stops a block while the defender controls a Forest")
    void grantedForestwalkPreventsBlock() {
        Permanent veldrane = addVeldrane();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new RysorianBadger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        veldrane.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(veldrane);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Without the ability Veldrane can be blocked even if the defender controls a Forest")
    void blockableWithoutActivation() {
        Permanent veldrane = addVeldrane();
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new RysorianBadger());

        veldrane.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(veldrane);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Forestwalk still allows a block when the defender controls no Forest")
    void forestwalkAllowsBlockWithoutMatchingForest() {
        Permanent veldrane = addVeldrane();
        harness.addMana(player1, ManaColor.BLACK, 3);
        Permanent blocker = addCreatureReady(player2, new RysorianBadger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        veldrane.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(veldrane);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Boost and forestwalk wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent veldrane = addVeldrane();
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = addCreatureReady(player2, new RysorianBadger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(veldrane.getPowerModifier()).isEqualTo(-3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(veldrane.getPowerModifier()).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, veldrane, Keyword.FORESTWALK)).isFalse();

        veldrane.setAttacking(true);
        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(veldrane);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addVeldrane();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with only colorless mana for the two black symbols")
    void cannotActivateWithOnlyColorlessMana() {
        addVeldrane();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
