package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AirMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to target Soldier")
    void abilityGrantsFlyingToSoldier() {
        addReadyAirMarshal(player1);
        Permanent soldier = addReadySoldier(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();

        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying is removed at end of turn")
    void flyingRemovedAtEndOfTurn() {
        addReadyAirMarshal(player1);
        Permanent soldier = addReadySoldier(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, soldier.getId());
        harness.passBothPriorities();
        assertThat(soldier.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(soldier.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a non-Soldier creature")
    void rejectsNonSoldierTarget() {
        addReadyAirMarshal(player1);
        Permanent nonSoldier = addReadyNonSoldier(player1);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonSoldier.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent addReadyAirMarshal(Player player) {
        Permanent permanent = new Permanent(new AirMarshal());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadySoldier(Player player) {
        Permanent permanent = new Permanent(new YotianSoldier());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyNonSoldier(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
