package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BattlefieldMedic;
import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaruSpiritualist.class, BattlefieldMedic.class, GiantGrowth.class, GrizzlyBears.class,
        ElaborateFirecannon.class})
class DaruSpiritualistTest extends BaseCardTest {

    @Test
    @DisplayName("A Cleric you control gets +0/+2 when targeted by a spell")
    void clericGetsToughnessFromSpellTargeting() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        harness.addToBattlefield(player1, new BattlefieldMedic());
        Permanent medic = findPermanent(player1, "Battlefield Medic");
        int powerBefore = gqs.getEffectivePower(gd, medic);
        int toughnessBefore = gqs.getEffectiveToughness(gd, medic);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, medic.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, medic)).isEqualTo(powerBefore + 3);
        assertThat(gqs.getEffectiveToughness(gd, medic)).isEqualTo(toughnessBefore + 5);
    }

    @Test
    @DisplayName("A Cleric you control gets +0/+2 when targeted by an ability")
    void clericGetsToughnessFromAbilityTargeting() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        Permanent spiritualist = findPermanent(player1, "Daru Spiritualist");
        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, spiritualist.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spiritualist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spiritualist)).isEqualTo(3);
        assertThat(spiritualist.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Clerics do not get the triggered toughness boost")
    void nonClericDoesNotGetBoost() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("The triggered toughness boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new DaruSpiritualist());
        Permanent spiritualist = findPermanent(player1, "Daru Spiritualist");

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, spiritualist.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spiritualist)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spiritualist)).isEqualTo(1);
    }
}
