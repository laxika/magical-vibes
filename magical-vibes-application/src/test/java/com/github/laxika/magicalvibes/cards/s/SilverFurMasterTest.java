package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HigureTheStillWind;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.cards.t.ThievesGuildEnforcer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilverFurMaster.class, NinjaOfTheDeepHours.class, GrizzlyBears.class,
        HigureTheStillWind.class, ThievesGuildEnforcer.class})
class SilverFurMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the generic cost of ninjutsu abilities by one")
    void reducesNinjutsuCost() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new SilverFurMaster());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new NinjaOfTheDeepHours()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not reduce ordinary activated abilities")
    void doesNotReduceOrdinaryActivatedAbilities() {
        Permanent master = addCreatureReady(player1, new SilverFurMaster());
        addCreatureReady(player1, new HigureTheStillWind());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, master.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Boosts other Ninja and Rogue creatures you control")
    void boostsOtherNinjasAndRogues() {
        Permanent master = addCreatureReady(player1, new SilverFurMaster());
        Permanent ninja = addCreatureReady(player1, new NinjaOfTheDeepHours());
        Permanent rogue = addCreatureReady(player1, new ThievesGuildEnforcer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentNinja = addCreatureReady(player2, new NinjaOfTheDeepHours());

        assertThat(gqs.computeStaticBonus(gd, master).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, ninja).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, ninja).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, rogue).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, rogue).toughness()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, bears).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opponentNinja).power()).isZero();
    }
}
