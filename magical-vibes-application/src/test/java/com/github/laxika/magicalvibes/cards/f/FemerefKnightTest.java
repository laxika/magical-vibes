package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
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

@CardUsed({FemerefKnight.class, IronTuskElephant.class, ZhalfirinKnight.class})
class FemerefKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new FemerefKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new IronTuskElephant());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent knight = addCreatureReady(player1, new FemerefKnight());
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving the ability grants vigilance until end of turn")
    void resolvingGrantsVigilance() {
        Permanent knight = addCreatureReady(player1, new FemerefKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOff() {
        Permanent knight = addCreatureReady(player1, new FemerefKnight());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without white mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new FemerefKnight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated repeatedly and does not tap the knight")
    void activationDoesNotTap() {
        Permanent knight = addCreatureReady(player1, new FemerefKnight());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }
}
