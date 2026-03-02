package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FuelForTheCauseTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Fuel for the Cause has CounterSpellEffect and ProliferateEffect")
    void hasCorrectProperties() {
        FuelForTheCause card = new FuelForTheCause();

        assertThat(card.isNeedsSpellTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(CounterSpellEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(ProliferateEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting a spell")
    void castingPutsOnStackTargetingSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        UUID bearsCardId = bears.getId();
        harness.castInstant(player2, 0, bearsCardId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry fuelEntry = gd.stack.getLast();
        assertThat(fuelEntry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(fuelEntry.getCard().getName()).isEqualTo("Fuel for the Cause");
        assertThat(fuelEntry.getTargetPermanentId()).isEqualTo(bearsCardId);
    }

    // ===== Resolving: counter =====

    @Test
    @DisplayName("Resolving counters target spell and puts it in owner's graveyard")
    void resolvingCountersTargetSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        // Counter resolves first; proliferate may await input
        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Fuel for the Cause goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fuel for the Cause"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Resolving: proliferate =====

    @Test
    @DisplayName("After countering, proliferate adds -1/-1 counter to chosen creature")
    void proliferateAddsMinusCountersAfterCounter() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        // Choose the bears with existing counter for proliferate
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("After countering, proliferate adds +1/+1 counter to chosen creature")
    void proliferateAddsPlusCountersAfterCounter() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setPlusOnePlusOneCounters(1);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(bears.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("Proliferate can choose none after countering")
    void proliferateCanChooseNoneAfterCounter() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setMinusOneMinusOneCounters(1);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("No proliferate prompt when no permanents have counters")
    void noProliferatePromptWhenNoCounters() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId());
        harness.passBothPriorities();

        // Spell countered, no proliferate needed — no eligible permanents
        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles entirely if target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new FuelForTheCause()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        // Remove Bears from stack before Fuel resolves
        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        // Entire spell fizzles — no counter, no proliferate
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fuel for the Cause"));
    }
}
