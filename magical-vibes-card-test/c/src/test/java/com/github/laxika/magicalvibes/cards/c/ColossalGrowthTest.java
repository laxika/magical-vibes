package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
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

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    void kickedGivesTargetCreaturePlusFourPlusFourTrampleAndHaste() {
        Permanent target = addCreature(player2);
        harness.setHand(player1, List.of(new ColossalGrowth()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(4);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
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

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
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
        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isZero();
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

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(3);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();

        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, target)).isEqualTo(7);
        assertThat(PermanentGrants.toughnessBoostOf(gd, target)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
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
