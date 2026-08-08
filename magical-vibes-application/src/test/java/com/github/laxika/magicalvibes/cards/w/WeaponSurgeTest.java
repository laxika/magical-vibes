package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeaponSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Grants +1/+0 and first strike to target creature you control")
    void pumpsTargetCreature() {
        Permanent bear = addCreature(player1);
        harness.setHand(player1, List.of(new WeaponSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addCreature(player1);
        Permanent theirs = addCreature(player2);
        harness.setHand(player1, List.of(new WeaponSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, theirs.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boost and first strike wear off at end of turn")
    void wearsOffAtCleanup() {
        Permanent bear = addCreature(player1);
        harness.setHand(player1, List.of(new WeaponSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Overloaded, every creature you control is pumped and no target is chosen")
    void overloadPumpsEveryCreatureYouControl() {
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player1);
        Permanent theirs = addCreature(player2);
        harness.setHand(player1, List.of(new WeaponSurge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(theirs.getPowerModifier()).isZero();
        assertThat(theirs.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Overload cannot be paid with only the normal mana cost available")
    void overloadRequiresTheFullOverloadCost() {
        addCreature(player1);
        harness.setHand(player1, List.of(new WeaponSurge()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithOverload(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
