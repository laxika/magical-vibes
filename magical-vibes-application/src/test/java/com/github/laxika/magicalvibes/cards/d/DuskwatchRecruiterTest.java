package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskwatchRecruiter.class, GrizzlyBears.class, LlanowarElves.class, Plains.class,
        Shock.class, SylvokLifestaff.class})
class DuskwatchRecruiterTest extends BaseCardTest {

    // ===== Front face: {2}{G} look at top three =====

    @Test
    @DisplayName("Activated ability offers creature cards among top three")
    void activatedAbilityOffersCreatures() {
        harness.setLibrary(player1, List.of(new LlanowarElves(), new Shock(), new Plains()));
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(player1, recruiter), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("Choosing a creature puts it into hand then orders rest on bottom")
    void choosingCreatureThenOrderingBottom() {
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(elves, shock, plains));
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(player1, recruiter), 0, null, null);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        int iShock = indexOfName(remaining, "Shock");
        int iPlains = indexOfName(remaining, "Plains");
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(iPlains, iShock)));

        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Plains", "Shock");
    }

    // ===== Werewolf transform: front → back =====

    @Test
    @DisplayName("Transforms to Krallenhorde Howler when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

        gd.spellsCastLastTurn.clear();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recruiter.isTransformed()).isTrue();
        assertThat(recruiter.getCard().getName()).isEqualTo("Krallenhorde Howler");
        assertThat(gqs.getEffectivePower(gd, recruiter)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, recruiter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

        gd.spellsCastLastTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(recruiter.isTransformed()).isFalse();
        assertThat(recruiter.getCard().getName()).isEqualTo("Duskwatch Recruiter");
    }

    // ===== Back face: creature cost reduction =====

    @Test
    @DisplayName("Krallenhorde Howler makes creature spells cost {1} less")
    void howlerReducesCreatureSpellCost() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

        // Transform first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(recruiter.isTransformed()).isTrue();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Grizzly Bears costs {1}{G} — with {1} reduction it should cost {G}
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Krallenhorde Howler does not reduce non-creature spell costs")
    void howlerDoesNotReduceNonCreatureCosts() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(recruiter.isTransformed()).isTrue();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Sylvok Lifestaff costs {1} — Howler's creature-only reduction must not make it free
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Werewolf transform: back → front =====

    @Test
    @DisplayName("Krallenhorde Howler transforms back when a player cast two or more spells last turn")
    void howlerTransformsBackWhenTwoSpellsCast() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(recruiter.isTransformed()).isTrue();

        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recruiter.isTransformed()).isFalse();
        assertThat(recruiter.getCard().getName()).isEqualTo("Duskwatch Recruiter");
        assertThat(gqs.getEffectivePower(gd, recruiter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, recruiter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining a creature keeps the unseen library above the ordered bottom cards")
    void mayDeclineCreatureAndOrderAllThreeOnBottom() {
        DuskwatchRecruiter creature = new DuskwatchRecruiter();
        Plains first = new Plains();
        Plains second = new Plains();
        Plains unseen = new Plains();
        harness.setLibrary(player1, List.of(creature, first, second, unseen));
        activateRecruiter();

        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, second, creature, first);
    }

    @Test
    @DisplayName("With no creatures, all three cards can be ordered on the bottom")
    void noCreaturesStillAllowsBottomOrdering() {
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        Plains unseen = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, unseen));
        activateRecruiter();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, third, second, first);
    }

    @Test
    @DisplayName("A library with only one creature still allows declining it")
    void mayDeclineOnlyCardInLibrary() {
        DuskwatchRecruiter creature = new DuskwatchRecruiter();
        harness.setLibrary(player1, List.of(creature));
        activateRecruiter();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or drawing a card")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        int handSize = gd.playerHands.get(player1.getId()).size();
        activateRecruiter();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("One spell from each player does not transform the Howler back")
    void oneSpellEachDoesNotSatisfyTwoSpellsByOnePlayer() {
        Permanent recruiter = transformRecruiter();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 1);
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(recruiter.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Howler does not reduce an opponent's creature spell cost")
    void costReductionIsControllerOnly() {
        transformRecruiter();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DuskwatchRecruiter()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Howler's reduction cannot pay a creature's colored mana requirement")
    void costReductionDoesNotRemoveColoredMana() {
        transformRecruiter();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DuskwatchRecruiter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Choosing among three creatures puts only one in hand")
    void selectsOnlyOneOfMultipleCreatures() {
        DuskwatchRecruiter first = new DuskwatchRecruiter();
        DuskwatchRecruiter second = new DuskwatchRecruiter();
        DuskwatchRecruiter third = new DuskwatchRecruiter();
        Plains unseen = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, unseen));
        activateRecruiter();

        harness.handleCardChosen(player1, 1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(second).doesNotContain(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unseen, third, first);
    }

    @Test
    @DisplayName("Recruiter transforms at the opponent's upkeep too")
    void transformsOnOpponentsUpkeep() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(recruiter.isTransformed()).isTrue();
        assertThat(recruiter.getCard().getName()).isEqualTo("Krallenhorde Howler");
    }

    private void activateRecruiter() {
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent transformRecruiter() {
        Permanent recruiter = harness.addToBattlefieldAndReturn(player1, new DuskwatchRecruiter());
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(recruiter.isTransformed()).isTrue();
        return recruiter;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }

    private int indexOfName(List<Card> cards, String name) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Card not found in list: " + name);
    }
}
