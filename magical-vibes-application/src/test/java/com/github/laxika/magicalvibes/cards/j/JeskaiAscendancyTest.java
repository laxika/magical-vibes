package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({JeskaiAscendancy.class, GrizzlyBears.class, Shock.class, Forest.class})
class JeskaiAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell boosts and untaps creatures you control")
    void noncreatureSpellBoostsAndUntapsCreatures() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.tap();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveToLootChoice();
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        Permanent opponentCreature = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The optional trigger draws then discards when accepted")
    void acceptedDrawTriggerLoots() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(new Shock(), bears));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castInstant(player1, 0, player2.getId());
        resolveToLootChoice();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger Jeskai Ascendancy")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The draw decision is made on resolution, not when the spell is cast")
    void lootChoiceWaitsForResolution() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("Declining the draw does not discard a card")
    void decliningDrawDoesNotDiscard() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new Shock(), bears));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveToLootChoice();
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card == bears);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger either ability")
    void opponentSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost and untap affect creatures present on resolution and expire at end of turn")
    void creaturesAreDeterminedAtResolutionAndBoostExpires() {
        harness.addToBattlefield(player1, new JeskaiAscendancy());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        Permanent arrivingBeforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        arrivingBeforeResolution.tap();
        resolveToLootChoice();
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        Permanent arrivingAfterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(arrivingBeforeResolution.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, arrivingBeforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, arrivingBeforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, arrivingAfterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, arrivingAfterResolution)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, arrivingBeforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, arrivingBeforeResolution)).isEqualTo(2);
    }

    private void resolveToLootChoice() {
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.ColorChoice choice) {
            harness.handleListChoice(player1, choice.options().getFirst());
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (int i = 0; i < 3 && gd.interaction.activeInteraction() == null; i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
