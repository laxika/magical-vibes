package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IronCrawCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts a target attacking creature by the Crusher's power")
    void attackBoostsTargetAttackerBySourcePower() {
        Permanent crusher = addCreatureReady(player1, new IronCrawCrusher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        crusher.setPowerModifier(2);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The prototype version uses its prototype power for the attack trigger")
    void prototypeUsesPrototypePower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IronCrawCrusher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent crusher = findPermanent(player1, "Iron-Craw Crusher");
        crusher.setSummoningSick(false);
        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, crusher)).containsExactly(CardColor.GREEN);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature that is not attacking")
    void rejectsNonAttackingTarget() {
        addCreatureReady(player1, new IronCrawCrusher());
        Permanent bystander = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
