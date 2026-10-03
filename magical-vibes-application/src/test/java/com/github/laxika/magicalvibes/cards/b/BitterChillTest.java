package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArmoryMice;
import com.github.laxika.magicalvibes.cards.c.CandyTrail;
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

@CardUsed({BitterChill.class, CandyTrail.class, ArmoryMice.class})
class BitterChillTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by tapping the enchanted creature and keeps it from untapping")
    void tapsAndLocksEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        harness.setHand(player1, List.of(new BitterChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {1} when the Aura enters a graveyard scries and draws")
    void paysToScryAndDraw() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        Permanent aura = attachAura(bears);
        Card topCard = new CandyTrail();
        Card nextCard = new ArmoryMice();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the graveyard trigger does not scry or draw")
    void decliningDoesNothing() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        Permanent aura = attachAura(bears);
        Card topCard = new CandyTrail();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CandyTrail());
        harness.setHand(player1, List.of(new BitterChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Entry trigger still taps the creature after the Aura is returned to hand")
    void entryTriggerUsesLastKnownAttachment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        harness.setHand(player1, List.of(new BitterChill()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Bitter Chill");
        assertThat(creature.isTapped()).isFalse();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aura));
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(aura.getCard());
    }

    @Test
    @DisplayName("Putting the scried card on the bottom draws the next card")
    void scryBottomBeforeDrawing() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        Permanent aura = attachAura(creature);
        Card topCard = new CandyTrail();
        Card nextCard = new ArmoryMice();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(nextCard).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The creature can untap once the Aura leaves the battlefield")
    void lockEndsWhenAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        Permanent aura = attachAura(creature);
        creature.setTapped(true);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aura));
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The graveyard trigger belongs to the Aura's last controller, not its owner")
    void graveyardTriggerUsesLastController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoryMice());
        BitterChill card = new BitterChill();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(creature.getId());
        Card topCard = new CandyTrail();
        Card nextCard = new ArmoryMice();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerHands.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nextCard);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("An Aura spell whose creature target leaves does not trigger scry or draw")
    void illegalTargetDoesNotTriggerGraveyardAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        BitterChill card = new BitterChill();
        harness.setHand(player1, List.of(card));
        Card topCard = new CandyTrail();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Losing the enchanted creature puts the Aura into the graveyard and triggers its ability")
    void creatureLeavingTriggersAuraGraveyardAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArmoryMice());
        Permanent aura = attachAura(creature);
        Card topCard = new CandyTrail();
        Card nextCard = new ArmoryMice();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BitterChill());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
