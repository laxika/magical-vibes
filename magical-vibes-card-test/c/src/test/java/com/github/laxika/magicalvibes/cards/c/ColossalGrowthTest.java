package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AutomaticLibrarian;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ColossalGrowth.class, AutomaticLibrarian.class, Forest.class})
class ColossalGrowthTest extends BaseCardTest {

    @Test
    void givesTargetCreaturePlusThreePlusThreeWithoutKicker() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ColossalGrowth()));
        addBaseMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void kickedGivesTargetCreaturePlusFourPlusFourTrampleAndHaste() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ColossalGrowth()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    void kickedEffectsExpireAtEndOfTurn() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ColossalGrowth()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ColossalGrowth()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void unkickedBoostExpiresAtEndOfTurn() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new ColossalGrowth()));
        addBaseMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    void kickedAndUnkickedSpellsOnTheStackKeepTheirOwnKickerStatus() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new ColossalGrowth(), new ColossalGrowth()));
        addKickedMana();
        addBaseMana();

        harness.castKickedInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(7);
        assertThat(target.getToughnessModifier()).isEqualTo(7);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AutomaticLibrarian());
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        addBaseMana();
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
