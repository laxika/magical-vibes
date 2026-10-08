package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinChirurgeon;
import com.github.laxika.magicalvibes.cards.g.GoblinGrenade;
import com.github.laxika.magicalvibes.cards.i.IcatianStore;
import com.github.laxika.magicalvibes.cards.o.Orgg;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({CombatMedic.class, GoblinChirurgeon.class, GoblinGrenade.class, IcatianStore.class, Orgg.class})
class CombatMedicTest extends BaseCardTest {

    private Permanent addMedicReady() {
        return addCreatureReady(player1, new CombatMedic());
    }

    private Permanent addActivationManaAndGrenade() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);
        return harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
    }

    @Test
    @DisplayName("Prevents 1 damage to a target creature")
    void preventsDamageToCreature() {
        addMedicReady();
        Permanent orgg = harness.addToBattlefieldAndReturn(player2, new Orgg());
        Permanent goblin = addActivationManaAndGrenade();

        harness.activateAbility(player1, 0, null, orgg.getId());
        harness.passBothPriorities();
        harness.castSorceryWithSacrifice(player1, 0, orgg.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(orgg);
        assertThat(orgg.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @CardUsed(ChandraNalaar.class)
    @DisplayName("Prevents 1 damage to a target planeswalker")
    void preventsDamageToPlaneswalker() {
        addMedicReady();
        Permanent chandra = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        chandra.setCounterCount(CounterType.LOYALTY, 10);
        Permanent goblin = addActivationManaAndGrenade();

        harness.activateAbility(player1, 0, null, chandra.getId());
        harness.passBothPriorities();
        harness.castSorceryWithSacrifice(player1, 0, chandra.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("Prevents 1 damage to a target player")
    void preventsDamageToPlayer() {
        addMedicReady();
        Permanent goblin = addActivationManaAndGrenade();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not tap itself when its ability is activated")
    void doesNotTapAsActivationCost() {
        Permanent medic = addMedicReady();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(medic.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addMedicReady();
        Permanent store = harness.addToBattlefieldAndReturn(player2, new IcatianStore());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, store.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations add their prevention shields")
    void repeatedActivationsStack() {
        addMedicReady();
        Permanent goblin = addActivationManaAndGrenade();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The shield is consumed by the first damage event")
    void shieldDoesNotPreventLaterDamage() {
        addMedicReady();
        Permanent firstGoblin = addActivationManaAndGrenade();
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinChirurgeon());
        harness.setHand(player1, List.of(new GoblinGrenade(), new GoblinGrenade()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), firstGoblin.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), secondGoblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("An unused shield expires at the end of the turn")
    void unusedShieldExpires() {
        addMedicReady();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player2, List.of(new CombatMedic()));
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinChirurgeon());
        harness.setHand(player2, List.of(new GoblinGrenade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player2, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent medic = harness.addToBattlefieldAndReturn(player1, new CombatMedic());
        medic.setSummoningSick(true);
        medic.tap();
        Permanent goblin = addActivationManaAndGrenade();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), goblin.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(medic.isTapped()).isTrue();
    }
}
