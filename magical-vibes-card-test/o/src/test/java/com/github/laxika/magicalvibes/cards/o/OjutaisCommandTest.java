package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjutaisCommand.class, GrizzlyBears.class, HillGiant.class, Spellbook.class})
class OjutaisCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a small creature and draws a card")
    void returnsSmallCreatureAndDrawsCard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castWithModes(new int[]{0, 3}, creature.getId(), List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gains 4 life and draws a card")
    void gainsLifeAndDrawsCard() {
        harness.setLife(player1, 10);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        castWithModes(new int[]{1, 3}, null, List.of());

        harness.assertLife(player1, 14);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a creature spell and gains 4 life")
    void countersCreatureSpellAndGainsLife() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player2, List.of(creature));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLife(player1, 10);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        castWithModes(new int[]{1, 2}, creature.getId(), List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Does not return a creature with mana value greater than 2")
    void returnModeRejectsCreatureWithHighManaValue() {
        HillGiant creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 3}, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter mode rejects a non-creature spell")
    void counterModeRejectsNonCreatureSpell() {
        Spellbook spellbook = new Spellbook();
        harness.setHand(player2, List.of(spellbook));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{2, 3}, spellbook.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a small creature and gains 4 life")
    void returnsSmallCreatureAndGainsLife() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 10);

        castWithModes(new int[]{0, 1}, creature.getId(), List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Returns a small creature and counters a creature spell")
    void returnsSmallCreatureAndCountersCreatureSpell() {
        GrizzlyBears deadCreature = new GrizzlyBears();
        GrizzlyBears creatureSpell = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(deadCreature));
        harness.setHand(player2, List.of(creatureSpell));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        castWithModes(new int[]{0, 2}, creatureSpell.getId(), List.of(deadCreature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a creature spell and draws a card")
    void countersCreatureSpellAndDrawsCard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player2, List.of(creature));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        castWithModes(new int[]{2, 3}, creature.getId(), List.of());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Return mode rejects a card in an opponent's graveyard")
    void returnModeRejectsOpponentsGraveyard() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 1}, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Return mode rejects a non-creature card with low mana value")
    void returnModeRejectsNonCreatureCard() {
        Spellbook artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 1}, artifact.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same mode twice")
    void rejectsRepeatedMode() {
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{1, 1}, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the return mode without a creature target")
    void returnModeRequiresTarget() {
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 1}, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the counter mode without a creature-spell target")
    void counterModeRequiresTarget() {
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{1, 2}, null, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not gain life when its only target leaves the graveyard")
    void doesNotGainLifeWhenOnlyTargetBecomesIllegal() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 1}, creature.getId(), List.of());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Ojutai's Command");
    }

    @Test
    @DisplayName("Does not draw when its creature-spell target is countered in response")
    void doesNotDrawWhenOnlySpellTargetBecomesIllegal() {
        GrizzlyBears creature = new GrizzlyBears();
        harness.setHand(player2, List.of(creature));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new OjutaisCommand(), new OjutaisCommand()));
        addMana();
        addMana();
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{2, 3}, creature.getId(), List.of());
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 2}, creature.getId(), List.of());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 14);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void castWithModes(int[] modes, java.util.UUID targetId, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new OjutaisCommand()));
        addMana();
        harness.castModalInstantWithModes(player1, 0, 2, modes, targetId, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
