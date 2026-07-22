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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuskwatchRecruiterTest extends BaseCardTest {

    // ===== Front face: {2}{G} look at top three =====

    @Test
    @DisplayName("Activated ability offers creature cards among top three")
    void activatedAbilityOffersCreatures() {
        setupTopThree(List.of(new LlanowarElves(), new Shock(), new Plains()));
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");
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
        setupTopThree(List.of(elves, shock, plains));
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, indexOf(player1, recruiter), 0, null, null);
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Llanowar Elves"));
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
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");

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
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");

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
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");

        // Transform first
        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(recruiter.isTransformed()).isTrue();

        // Grizzly Bears costs {1}{G} — with {1} reduction it should cost {G}
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Krallenhorde Howler does not reduce non-creature spell costs")
    void howlerDoesNotReduceNonCreatureCosts() {
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");

        gd.spellsCastLastTurn.clear();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(recruiter.isTransformed()).isTrue();

        // Sylvok Lifestaff costs {1} — Howler's creature-only reduction must not make it free
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Werewolf transform: back → front =====

    @Test
    @DisplayName("Krallenhorde Howler transforms back when a player cast two or more spells last turn")
    void howlerTransformsBackWhenTwoSpellsCast() {
        harness.addToBattlefield(player1, new DuskwatchRecruiter());
        Permanent recruiter = findPermanent(player1, "Duskwatch Recruiter");

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

    private void setupTopThree(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
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
