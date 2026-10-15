package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightErrantOfEos.class, GrizzlyBears.class, Shock.class,
        LlanowarElves.class, Ornithopter.class})
class KnightErrantOfEosTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers up to two creatures within the convoke count mana value")
    void etbUsesConvokeCountForSelection() {
        GrizzlyBears firstEligible = new GrizzlyBears();
        GrizzlyBears secondEligible = new GrizzlyBears();
        GrizzlyBears thirdEligible = new GrizzlyBears();
        KnightErrantOfEos tooExpensive = new KnightErrantOfEos();
        harness.setLibrary(player1, List.of(firstEligible, secondEligible, tooExpensive,
                thirdEligible, new Shock(), new Shock()));

        Permanent firstConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondConvokeCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KnightErrantOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(firstConvokeCreature.getId(), secondConvokeCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                firstEligible.getId(), secondEligible.getId(), thirdEligible.getId())
                .doesNotContain(tooExpensive.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(firstEligible.getId(), secondEligible.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(firstEligible, secondEligible)
                .doesNotContain(tooExpensive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB excludes positive-mana-value creatures when none convoked it")
    void etbWithNoConvokeOffersNoSelection() {
        GrizzlyBears topCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCreature, new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new KnightErrantOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCreature);
        assertThat(gd.playerDecks.get(player1.getId())).contains(topCreature);
    }

    @Test
    @DisplayName("One convoking creature permits selecting two creatures of mana value one")
    void oneConvokeStillAllowsTwoCards() {
        LlanowarElves first = new LlanowarElves();
        LlanowarElves second = new LlanowarElves();
        GrizzlyBears tooExpensive = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, tooExpensive,
                new Shock(), new Shock(), new Shock()));
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KnightErrantOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).contains(tooExpensive).hasSize(4);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Without convoke two zero-mana creatures may still be revealed and taken")
    void noConvokeAllowsZeroManaCreatures() {
        Ornithopter first = new Ornithopter();
        Ornithopter second = new Ornithopter();
        LlanowarElves tooExpensive = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, tooExpensive,
                new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of(new KnightErrantOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).contains(tooExpensive).hasSize(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may decline all eligible cards from a short library")
    void mayDeclineAllCardsFromShortLibrary() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        Permanent convoker1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent convoker2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KnightErrantOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(convoker1.getId(), convoker2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only the top six cards are eligible and choosing just one is allowed")
    void onlyTopSixAreEligibleAndOneMayBeChosen() {
        GrizzlyBears eligible = new GrizzlyBears();
        GrizzlyBears seventh = new GrizzlyBears();
        harness.setLibrary(player1, List.of(eligible, new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), seventh));
        Permanent convoker1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent convoker2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KnightErrantOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(),
                List.of(convoker1.getId(), convoker2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible);
        assertThat(gd.playerDecks.get(player1.getId())).contains(seventh).hasSize(6);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
