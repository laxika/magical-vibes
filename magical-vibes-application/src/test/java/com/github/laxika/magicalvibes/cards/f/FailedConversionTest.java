package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.w.WarHistorian;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FailedConversion.class, ColossalDreadmaw.class, Forest.class, WarHistorian.class})
class FailedConversionTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -4/-4")
    void enchantedCreatureGetsDebuff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FailedConversion());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("When enchanted creature dies, the Aura's controller surveils two")
    void enchantedCreatureDeathSurveilsTwo() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ColossalDreadmaw());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FailedConversion());
        aura.setAttachedTo(creature.getId());
        Card topCard = new ColossalDreadmaw();
        Card secondCard = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.playerId()).isEqualTo(player1.getId());
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondCard);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FailedConversion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Killing an opposing creature with the Aura lets the Aura's controller reorder both cards")
    void auraControllerSurveilsWhenDebuffKillsOpposingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WarHistorian());
        Card aura = new FailedConversion();
        Card first = new Forest();
        Card second = new FailedConversion();
        Card third = new Forest();
        Card opponentTop = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentTop));
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.playerId()).isEqualTo(player1.getId());
        assertThat(surveil.cards()).containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsIntoGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WarHistorian());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FailedConversion());
        aura.setAttachedTo(creature.getId());
        Card first = new Forest();
        Card second = new FailedConversion();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.runStateBasedActions();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    void surveilWithOneCardUsesOnlyAvailableCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WarHistorian());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FailedConversion());
        aura.setAttachedTo(creature.getId());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.runStateBasedActions();
        harness.passBothPriorities();

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
    }

    @Test
    void surveilWithEmptyLibraryFinishesWithoutChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WarHistorian());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FailedConversion());
        aura.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of());

        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
