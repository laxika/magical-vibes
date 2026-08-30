package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AysenAbbey;
import com.github.laxika.magicalvibes.cards.r.RysorianBadger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Torture.class, RysorianBadger.class, AysenAbbey.class})
class TortureTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Torture attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent badgerPerm = addCreatureReady(player1, new RysorianBadger());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        gs.playCard(gd, player1, 0, 0, badgerPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Torture")
                        && p.isAttached()
                        && p.getAttachedTo().equals(badgerPerm.getId()));
    }

    @Test
    @DisplayName("Activating ability puts a -1/-1 counter on the enchanted creature")
    void activatingPutsMinusCounter() {
        Permanent badgerPerm = addCreatureReady(player1, new RysorianBadger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(badgerPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura at index 1 (badger at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(badgerPerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(badgerPerm.getEffectivePower()).isEqualTo(1);
        assertThat(badgerPerm.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent badgerPerm = addCreatureReady(player1, new RysorianBadger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(badgerPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Torture");
    }

    @Test
    @DisplayName("Ability can be activated multiple times, stacking -1/-1 counters")
    void abilityStacksCounters() {
        Permanent badgerPerm = addCreatureReady(player1, new RysorianBadger());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(badgerPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(badgerPerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and shrink it")
    void canEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new RysorianBadger());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        gs.playCard(gd, player1, 0, 0, opponentCreature.getId(), null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent auraPerm = findPermanent(player1, "Torture");
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(auraPerm);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new AysenAbbey());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, land.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability does nothing when Torture is no longer attached")
    void abilityDoesNothingWhenAuraBecomesUnattached() {
        Permanent badgerPerm = addCreatureReady(player1, new RysorianBadger());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(badgerPerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        auraPerm.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(badgerPerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
