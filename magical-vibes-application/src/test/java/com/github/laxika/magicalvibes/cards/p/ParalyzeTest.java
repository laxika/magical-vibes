package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Paralyze.class, GrizzlyBears.class, SolRing.class})
class ParalyzeTest extends BaseCardTest {

    // ===== ETB tap =====

    @Test
    @DisplayName("Resolving Paralyze taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        assertThat(creature.isTapped()).isFalse();

        harness.setHand(player1, List.of(new Paralyze()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Paralyze")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Paralyze cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setHand(player1, List.of(new Paralyze()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The ETB tap uses the creature currently enchanted when it resolves")
    void etbTapsCurrentEnchantedCreature() {
        Permanent originalTarget = addCreatureReady(player2, new GrizzlyBears());
        Permanent currentEnchantedCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Paralyze()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, originalTarget.getId());
        harness.passBothPriorities(); // resolve the Aura spell

        Permanent aura = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Paralyze"))
                .findFirst()
                .orElseThrow();
        aura.setAttachedTo(currentEnchantedCreature.getId());

        harness.passBothPriorities(); // resolve the ETB trigger

        assertThat(originalTarget.isTapped()).isFalse();
        assertThat(currentEnchantedCreature.isTapped()).isTrue();
    }

    // ===== Doesn't untap =====

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        attachParalyze(creature);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Upkeep: may pay {4} to untap =====

    @Test
    @DisplayName("Enchanted creature's controller pays {4} at upkeep to untap it")
    void controllerPaysToUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.addMana(player2, ManaColor.BLACK, 4); // mana available at payment time
        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the {4} payment leaves the creature tapped")
    void decliningLeavesTapped() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.addMana(player2, ManaColor.BLACK, 4); // could pay, but declines
        harness.handleMayAbilityChosen(player2, false);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting without enough mana leaves the creature tapped")
    void cannotPayLeavesTapped() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        // No mana for player2.
        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        harness.handleMayAbilityChosen(player2, true);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Upkeep untap ability does not fire during the Aura controller's upkeep")
    void doesNotTriggerDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        attachParalyze(creature);

        harness.addMana(player1, ManaColor.BLACK, 4);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // No may-pay prompt for player1; creature stays tapped.
        assertThat(creature.isTapped()).isTrue();
    }

    // ===== Helpers =====

    private void attachParalyze(Permanent creature) {
        Permanent aura = new Permanent(new Paralyze());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
