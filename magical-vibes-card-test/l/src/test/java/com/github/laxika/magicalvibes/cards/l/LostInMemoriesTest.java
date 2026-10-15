package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LostInMemories.class, GrizzlyBears.class, GiantGrowth.class, LavaAxe.class, ThinkTwice.class, DeepAnalysis.class, Naturalize.class})
class LostInMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Lost in Memories boosts the enchanted creature")
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage grants flashback to an instant or sorcery in the controller's graveyard")
    void combatDamageGrantsFlashback() {
        GiantGrowth spell = new GiantGrowth();
        harness.setGraveyard(player1, List.of(spell));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).containsExactly(spell.getId());
    }

    @Test
    @DisplayName("The granted flashback uses the card's mana cost")
    void grantedFlashbackUsesManaCost() {
        GiantGrowth spell = new GiantGrowth();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        harness.setGraveyard(player1, List.of(spell));
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveFlashback(player1, 0, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Lost in Memories cannot enchant an opponent's creature")
    void cannotEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LostInMemories()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    void flashbackUsesExactlyOneGreenAndExilesTheInstant() {
        GiantGrowth spell = new GiantGrowth();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveFlashback(player1, 0, creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void sorceryGainsFlashbackButRetainsSorceryTiming() {
        LavaAxe spell = new LavaAxe();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castAndResolveFlashback(player1, 0, player2.getId());

        harness.assertLife(player2, lifeBefore - 5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void targetMustBeAnInstantOrSorceryInYourGraveyardAndCannotBeDeclined() {
        GiantGrowth spell = new GiantGrowth();
        GiantGrowth opposingSpell = new GiantGrowth();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell, new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(opposingSpell));
        attachAura(creature);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.MultiGraveyardChoice.class, choice -> {
                    assertThat(choice.cards()).containsExactly(spell);
                    assertThat(choice.minCount()).isEqualTo(1);
                    assertThat(choice.maxCount()).isEqualTo(1);
                });
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).containsExactly(spell.getId());
    }

    @Test
    void combatWithNoLegalGraveyardTargetDoesNotPrompt() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        attachAura(creature);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).isEmpty();
    }

    @Test
    void anotherCreaturesCombatDamageDoesNotGrantFlashback() {
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));
        attachAura(enchanted);

        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).isEmpty();
    }

    @Test
    void flashbackGrantExpiresAtEndOfTurn() {
        GiantGrowth spell = new GiantGrowth();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(spell.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).doesNotContain(spell.getId());
        assertThatThrownBy(() -> harness.castFlashback(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedFlashbackIsAvailableWhenPrintedFlashbackIsUnaffordable() {
        ThinkTwice spell = new ThinkTwice();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void canFlashInAuraDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);
        attachAura(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Lost in Memories");
    }

    @Test
    void grantedFlashbackDoesNotRequirePrintedFlashbackLifePayment() {
        DeepAnalysis spell = new DeepAnalysis();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 1);
        attachAura(creature);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.ensurePriority(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveFlashback(player1, 0, player1.getId());

        harness.assertLife(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void destroyingAuraDoesNotRemoveAnAlreadyTriggeredAbility() {
        GiantGrowth spell = new GiantGrowth();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        attachAura(creature);
        Permanent aura = findPermanent(player1, "Lost in Memories");

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lost in Memories");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(spell.getId());
    }

    private void attachAura(Permanent creature) {
        harness.setHand(player1, List.of(new LostInMemories()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
