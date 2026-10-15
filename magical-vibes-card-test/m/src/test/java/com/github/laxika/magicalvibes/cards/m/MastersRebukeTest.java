package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.t.TamiyoCompleatedSage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MastersRebuke.class, HillGiant.class, GrizzlyBears.class,
        TamiyoCompleatedSage.class, JayaVeneratedFiremage.class})
class MastersRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("A creature you control deals its power to an opposing creature")
    void dealsPowerDamageToCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(
                source.getId(), harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature you control deals its power to an opposing planeswalker")
    void dealsPowerDamageToPlaneswalker() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent planeswalker = addPlaneswalker(player2, 5);
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void cannotTargetOwnCreatureSecond() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("don't control");
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new TamiyoCompleatedSage());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }

    @Test
    void usesPowerAtResolution() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = addPlaneswalker(player2, 5);
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void dealsNoDamageWhenSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Master's Rebuke");
    }

    @Test
    void dealsNoDamageWhenSourceChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void dealsNoDamageWhenVictimChangesController() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void cannotUseOpposingCreatureAsDamageSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void appliesDamageBonusForTheRedCreatureRatherThanTheGreenSpell() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent jaya = harness.addToBattlefieldAndReturn(player1, new JayaVeneratedFiremage());
        jaya.setCounterCount(CounterType.LOYALTY, 5);
        Permanent target = addPlaneswalker(player2, 5);
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void victimDoesNotDealDamageBack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MastersRebuke()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(source.getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }
}
