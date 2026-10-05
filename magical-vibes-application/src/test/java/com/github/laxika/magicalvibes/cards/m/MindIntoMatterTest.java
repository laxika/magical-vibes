package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InklingMascot;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindIntoMatter.class, GrizzlyBears.class, InklingMascot.class, Island.class, Pacifism.class})
class MindIntoMatterTest extends BaseCardTest {

    @Test
    @DisplayName("With X=2, draws two and can put a mana-value-2 permanent onto the battlefield tapped")
    void drawsXAndPutsPermanentTapped() {
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new MindIntoMatter(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 2);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Grizzly Bears is at hand index 0 (drawn cards were appended after it).
        harness.handleCardChosen(player1, 0);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("With X=0, a mana-value-2 permanent is not eligible to be put onto the battlefield")
    void manaValueBoundedByX() {
        harness.setHand(player1, List.of(new MindIntoMatter(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void canDeclineAfterDrawing() {
        harness.setHand(player1, List.of(new MindIntoMatter(), new InklingMascot()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Inkling Mascot");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Mind into Matter");
    }

    @Test
    void canChooseNewlyDrawnPermanentAndPutsOnlyOneCard() {
        harness.setHand(player1, List.of(new MindIntoMatter()));
        harness.setLibrary(player1, List.of(new InklingMascot(), new InklingMascot(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Inkling Mascot").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zeroXCanPutLandTappedWithoutDrawing() {
        harness.setHand(player1, List.of(new MindIntoMatter(), new Island()));
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
        harness.assertNotInHand(player1, "Island");
    }

    @Test
    void auraWithoutLegalEnchanteeStaysInHand() {
        harness.setHand(player1, List.of(new MindIntoMatter(), new Pacifism()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    void auraEntersAttachedToChosenCreature() {
        harness.addToBattlefield(player2, new InklingMascot());
        Permanent creature = findPermanent(player2, "Inkling Mascot");
        harness.setHand(player1, List.of(new MindIntoMatter(), new Pacifism()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, creature.getId());
        Permanent aura = findPermanent(player1, "Pacifism");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(aura.isTapped()).isTrue();
    }

    @Test
    void rejectsNonpermanentAndPermanentAbovePositiveX() {
        harness.setHand(player1, List.of(new MindIntoMatter(), new MindIntoMatter(),
                new InklingMascot(), new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 2);

        harness.assertInHand(player1, "Mind into Matter");
        harness.assertInHand(player1, "Inkling Mascot");
        assertThat(findPermanent(player1, "Island").isTapped()).isTrue();
    }
}
