package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.k.KilnFiend;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurrakarSpellblade.class, Forest.class, Staggershock.class, KilnFiend.class,
        FlameSlash.class, HeatRay.class})
class SurrakarSpellbladeTest extends BaseCardTest {

    @Test
    @DisplayName("May put a charge counter on itself when its controller casts an instant")
    void mayPutChargeCounterOnInstantCast() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(spellblade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not trigger the charge-counter ability")
    void creatureCastDoesNotTrigger() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player1, List.of(new KilnFiend()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(spellblade.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("May draw cards equal to its charge counters after dealing combat damage")
    void mayDrawCardsEqualToChargeCounters() {
        Permanent spellblade = addReadySpellblade(player1);
        spellblade.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());
        spellblade.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the combat-damage draw leaves the library unchanged")
    void mayDeclineCombatDamageDraw() {
        Permanent spellblade = addReadySpellblade(player1);
        spellblade.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, new ArrayList<>(List.of(new Forest(), new Forest())));
        harness.setHand(player1, new ArrayList<>());
        spellblade.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void sorceryCastMayAddChargeCounterAtResolution() {
        Permanent spellblade = addReadySpellblade(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KilnFiend());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, target.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spellblade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void mayDeclineChargeCounterAtResolution() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player1, List.of(new Staggershock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(spellblade.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsInstantDoesNotAddChargeCounter() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player2, List.of(new Staggershock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(spellblade.getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    void zeroChargeCountersDrawNoCards() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        spellblade.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void drawUsesCounterCountAtResolution() {
        Permanent spellblade = addReadySpellblade(player1);
        spellblade.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        spellblade.setAttacking(true);

        resolveCombat();
        spellblade.setCounterCount(CounterType.CHARGE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawUsesLastKnownCountersWhenSourceDiesInResponse() {
        Permanent spellblade = addReadySpellblade(player1);
        spellblade.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new HeatRay()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        spellblade.setAttacking(true);

        resolveCombat();
        harness.castInstant(player2, 0, 1, spellblade.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Surrakar Spellblade");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
    private Permanent addReadySpellblade(Player player) {
        Permanent spellblade = harness.addToBattlefieldAndReturn(player, new SurrakarSpellblade());
        spellblade.setSummoningSick(false);
        return spellblade;
    }
}
