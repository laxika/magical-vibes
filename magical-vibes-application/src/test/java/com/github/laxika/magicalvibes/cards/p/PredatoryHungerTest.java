package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.Allay;
import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.cards.e.EnchantmentAlteration;
import com.github.laxika.magicalvibes.cards.f.FightingChance;
import com.github.laxika.magicalvibes.cards.f.Forbid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatoryHunger.class, ElvishBerserker.class, FightingChance.class, Allay.class, Forbid.class})
class PredatoryHungerTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a creature spell puts a +1/+1 counter on the enchanted creature")
    void opponentCastingCreatureSpellAddsCounter() {
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        enchantHost(host);

        castOpponentCreatureSpell();
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent casting a noncreature spell does not trigger Predatory Hunger")
    void opponentCastingNoncreatureSpellDoesNotAddCounter() {
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        enchantHost(host);

        prepareMainPhase(player2);
        harness.castFromHand(player2, new FightingChance(), "{R}");
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller casting a creature spell does not trigger Predatory Hunger")
    void controllerCastingCreatureSpellDoesNotAddCounter() {
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        enchantHost(host);

        prepareMainPhase(player1);
        harness.castFromHand(player1, new ElvishBerserker(), "{G}");
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An enchanted creature controlled by an opponent gets the counter")
    void opponentControlledEnchantedCreatureGetsCounter() {
        Permanent host = addCreatureReady(player2, new ElvishBerserker());
        enchantHost(host);

        castOpponentCreatureSpell();
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each opponent creature spell creates its own counter trigger")
    void eachOpponentCreatureSpellAddsCounter() {
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        enchantHost(host);

        castOpponentCreatureSpell();
        resolveAllTriggers();
        castOpponentCreatureSpell();
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The trigger still resolves when the creature spell is countered")
    void creatureSpellBeingCounteredDoesNotUndoTrigger() {
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        enchantHost(host);

        ElvishBerserker creatureSpell = castOpponentCreatureSpell();
        harness.setHand(player1, List.of(new Forbid()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creatureSpell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Elvish Berserker");
    }

    @Test
    @DisplayName("The trigger uses the last enchanted creature if the Aura leaves before resolution")
    void triggerUsesLastEnchantedCreatureAfterAuraLeaves() {
        Permanent host = addCreatureReady(player1, new ElvishBerserker());
        enchantHost(host);
        Permanent aura = findPermanent(player1, "Predatory Hunger");

        castOpponentCreatureSpell();
        harness.setHand(player1, List.of(new Allay()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(host.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Predatory Hunger");
    }

    @Test
    @CardUsed(EnchantmentAlteration.class)
    @DisplayName("The trigger affects the creature enchanted when it resolves")
    void triggerUsesCurrentEnchantedCreatureAtResolution() {
        Permanent firstHost = addCreatureReady(player1, new ElvishBerserker());
        Permanent secondHost = addCreatureReady(player1, new ElvishBerserker());
        Permanent thirdHost = addCreatureReady(player2, new ElvishBerserker());
        enchantHost(firstHost);
        Permanent aura = findPermanent(player1, "Predatory Hunger");

        castOpponentCreatureSpell();
        harness.setHand(player1, List.of(new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(secondHost.getId(), thirdHost.getId()).doesNotContain(firstHost.getId());
        harness.handlePermanentChosen(player1, secondHost.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(secondHost.getId());
        harness.passBothPriorities();

        assertThat(firstHost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(secondHost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void enchantHost(Permanent host) {
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new PredatoryHunger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, host.getId());
        harness.passBothPriorities();
    }

    private ElvishBerserker castOpponentCreatureSpell() {
        ElvishBerserker creatureSpell = new ElvishBerserker();
        prepareMainPhase(player2);
        harness.castFromHand(player2, creatureSpell, "{G}");
        return creatureSpell;
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
