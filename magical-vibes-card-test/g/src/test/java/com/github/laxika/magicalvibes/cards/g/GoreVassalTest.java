package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MelirasKeepers;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.cards.p.PlagueMyr;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoreVassal.class, PhyrexianRager.class, PlagueMyr.class, GoForTheThroat.class, MelirasKeepers.class})
class GoreVassalTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Gore Vassal and puts ability on stack")
    void activatingSacrificesAndPutsOnStack() {
        addCreatureReady(player1, new GoreVassal());
        harness.addToBattlefield(player2, new PhyrexianRager());

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Rager");
        harness.activateAbility(player1, 0, null, targetId);

        // Gore Vassal should be sacrificed
        harness.assertNotOnBattlefield(player1, "Gore Vassal");
        harness.assertInGraveyard(player1, "Gore Vassal");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Gore Vassal");
    }

    @Test
    @DisplayName("Puts -1/-1 counter on 2/2 creature and regenerates it (toughness becomes 1)")
    void putsCounterAndRegeneratesOnSurvivingCreature() {
        addCreatureReady(player1, new GoreVassal());
        harness.addToBattlefield(player2, new PhyrexianRager());

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Rager");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Phyrexian Rager");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts -1/-1 counter on 1/1 creature, does not regenerate (toughness becomes 0), creature dies")
    void doesNotRegenerateWhenToughnessDropsToZero() {
        addCreatureReady(player1, new GoreVassal());
        harness.addToBattlefield(player2, new PlagueMyr());

        UUID targetId = harness.getPermanentId(player2, "Plague Myr");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        // Plague Myr should be dead (0 toughness, no regen shield to save it)
        harness.assertNotOnBattlefield(player2, "Plague Myr");
        harness.assertInGraveyard(player2, "Plague Myr");
    }

    @Test
    @DisplayName("Can target own creature to give it a regeneration shield")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new GoreVassal());
        harness.addToBattlefield(player1, new PhyrexianRager());

        UUID targetId = harness.getPermanentId(player1, "Phyrexian Rager");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Phyrexian Rager");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        addCreatureReady(player1, new GoreVassal());
        harness.addToBattlefield(player2, new PhyrexianRager());

        UUID targetId = harness.getPermanentId(player2, "Phyrexian Rager");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regenerates a creature even when counters cannot be put on it")
    void regeneratesWhenCounterPlacementIsImpossible() {
        harness.addToBattlefield(player1, new GoreVassal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MelirasKeepers());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate while summoning sick and tapped because the cost only sacrifices")
    void canActivateWhileSummoningSickAndTapped() {
        Permanent vassal = harness.addToBattlefieldAndReturn(player1, new GoreVassal());
        vassal.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianRager());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gore Vassal");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target itself but the sacrificed target is illegal at resolution")
    void canTargetItself() {
        Permanent vassal = harness.addToBattlefieldAndReturn(player1, new GoreVassal());

        harness.activateAbility(player1, 0, null, vassal.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gore Vassal");
        harness.assertInGraveyard(player1, "Gore Vassal");
        assertThat(gd.stack).isEmpty();
        assertThat(vassal.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(vassal.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("The regeneration shield replaces subsequent destruction")
    void shieldReplacesDestruction() {
        harness.addToBattlefield(player1, new GoreVassal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianRager());
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Phyrexian Rager");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The shield is created before lethal marked damage is checked")
    void regeneratesBeforeLethalDamageStateBasedAction() {
        harness.addToBattlefield(player1, new GoreVassal());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianRager());
        target.setMarkedDamage(1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Phyrexian Rager");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isTapped()).isTrue();
    }
}
