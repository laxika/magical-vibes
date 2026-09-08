package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TenacityTest extends BaseCardTest {

    @Test
    @DisplayName("Tenacity untaps, boosts, and grants lifelink to your creatures")
    void untapsBoostsAndGrantsLifelinkToOwnCreatures() {
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        mine.tap();
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        theirs.tap();

        castTenacity();

        assertThat(mine.isTapped()).isFalse();
        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(3);
        assertThat(mine.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(theirs.isTapped()).isTrue();
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Tenacity's lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castTenacity();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Tenacity's effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castTenacity();
        assertThat(creature.getEffectivePower()).isEqualTo(3);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    private void castTenacity() {
        harness.setHand(player1, List.of(new Tenacity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
