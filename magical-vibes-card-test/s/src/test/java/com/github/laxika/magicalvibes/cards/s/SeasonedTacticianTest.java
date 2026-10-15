package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AesthirGlider;
import com.github.laxika.magicalvibes.cards.g.GuerrillaTactics;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeasonedTactician.class, AesthirGlider.class, GuerrillaTactics.class, MindRot.class})
class SeasonedTacticianTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {3} and exiling the top four library cards prompts for a source choice")
    void activateExilesTopFourAndPromptsForSource() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        GameData gd = harness.getGameData();
        addCreatureReady(player2, new AesthirGlider());

        List<Card> library = gd.playerDecks.get(player1.getId());
        List<Card> topFour = List.copyOf(library.subList(0, 4));
        int deckBefore = library.size();
        int exileBefore = gd.exiledCards.size();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
        assertThat(gd.exiledCards).hasSize(exileBefore + 4);
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .containsAll(topFour.stream().map(Card::getId).toList());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source")
    void preventsDamageFromChosenSource() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeasonedTactician());
        Permanent goblin = addCreatureReady(player2, new AesthirGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeasonedTactician());
        Permanent chosen = addCreatureReady(player2, new AesthirGlider());
        Permanent other = addCreatureReady(player2, new AesthirGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    void preventsDamageFromChosenSpell() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new SeasonedTactician());
        GuerrillaTactics spell = new GuerrillaTactics();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(spell.getId());

        harness.handlePermanentChosen(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with fewer than four cards in library")
    void cannotActivateWithTooFewLibraryCards() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        harness.setLibrary(player1, List.copyOf(gd.playerDecks.get(player1.getId()).subList(0, 3)));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough cards in library to exile");
    }

    @Test
    void paysExileCostImmediatelyWithExactlyFourCards() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        List<Card> cards = List.of(new AesthirGlider(), new AesthirGlider(),
                new AesthirGlider(), new AesthirGlider());
        harness.setLibrary(player1, cards);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getId())
                .containsAll(cards.stream().map(Card::getId).toList());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent tactician = harness.addToBattlefieldAndReturn(player1, new SeasonedTactician());
        tactician.tap();
        tactician.setSummoningSick(true);
        Permanent source = addCreatureReady(player2, new AesthirGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());
        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    void unusedShieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        Permanent source = addCreatureReady(player2, new AesthirGlider());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        source.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    void canChooseDiscardedCardWhoseDamageTriggerIsOnStack() {
        harness.addToBattlefield(player1, new SeasonedTactician());
        GuerrillaTactics tactics = new GuerrillaTactics();
        harness.setHand(player2, List.of(tactics, new AesthirGlider()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(tactics.getId());
        harness.handlePermanentChosen(player1, tactics.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }

}
