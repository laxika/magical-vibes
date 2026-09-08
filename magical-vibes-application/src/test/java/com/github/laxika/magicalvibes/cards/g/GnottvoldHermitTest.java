package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ChromeHostHulk;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GnottvoldHermit.class, ChromeHostHulk.class, GrizzlyBears.class})
class GnottvoldHermitTest extends BaseCardTest {

    @Test
    void transformsWithPhyrexianManaAbility() {
        Permanent hermit = addCreatureReady(player1, new GnottvoldHermit());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(hermit.getCard().getName()).isEqualTo("Chrome Host Hulk");
        assertThat(hermit.isTransformed()).isTrue();
    }

    @Test
    void attackTriggerSetsAnotherCreatureToFiveFiveUntilEndOfTurn() {
        Permanent hulk = addCreatureReady(player1, new GnottvoldHermit());
        hulk.setCard(hulk.getOriginalCard().getBackFaceCard());
        hulk.setTransformed(true);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(5);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void attackTriggerCannotTargetTheHulkItself() {
        Permanent hulk = addCreatureReady(player1, new GnottvoldHermit());
        hulk.setCard(hulk.getOriginalCard().getBackFaceCard());
        hulk.setTransformed(true);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hulk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
