package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SurrakarSpellbladeTest extends BaseCardTest {

    @Test
    @DisplayName("May put a charge counter on itself when its controller casts an instant")
    void mayPutChargeCounterOnInstantCast() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spellblade.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not trigger the charge-counter ability")
    void creatureCastDoesNotTrigger() {
        Permanent spellblade = addReadySpellblade(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
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

    private Permanent addReadySpellblade(Player player) {
        Permanent spellblade = new Permanent(new SurrakarSpellblade());
        spellblade.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spellblade);
        return spellblade;
    }
}
