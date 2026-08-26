package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JoinTheDead.class, ColossalDreadmaw.class, GrizzlyBears.class, Shock.class})
class JoinTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -5/-5 below descend 4")
    void givesMinusFiveMinusFiveBelowDescendFour() {
        Permanent target = addTarget();
        castJoinTheDead(target);

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives target creature -10/-10 with four permanent cards in the graveyard")
    void givesMinusTenMinusTenAtDescendFour() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent target = addTarget();

        castJoinTheDead(target);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Nonpermanent cards do not count toward descend 4")
    void nonpermanentCardsDoNotCount() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        Permanent target = addTarget();

        castJoinTheDead(target);

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Descend 4 is checked when the spell resolves")
    void descendFourIsCheckedAtResolution() {
        Permanent target = addTarget();
        harness.setHand(player1, List.of(new JoinTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private Permanent addTarget() {
        return harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
    }

    private void castJoinTheDead(Permanent target) {
        harness.setHand(player1, List.of(new JoinTheDead()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
