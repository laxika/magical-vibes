package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuperiorFoesOfSpiderMan.class, Forest.class, HillGiant.class, GrizzlyBears.class, Blaze.class})
class SuperiorFoesOfSpiderManTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater may exile the top card and play it")
    void qualifyingSpellExilesTopCardWithPlayPermission() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Declining the optional exile leaves the top card in the library")
    void decliningExileLeavesTopCardInLibrary() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not trigger the ability")
    void lowManaValueSpellDoesNotTrigger() {
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }
    @Test
    void anotherExileRevokesOnlyThePreviousCardsPermission() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());

        castQualifyingSpellAndChoose(true);
        castQualifyingSpellAndChoose(true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.castFromExile(player1, second.getId());
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first).doesNotContain(second);
    }

    @Test
    void decliningAnotherExileKeepsThePreviousCardPlayable() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());

        castQualifyingSpellAndChoose(true);
        castQualifyingSpellAndChoose(false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void emptyLibraryDoesNotRevokeThePreviousPermission() {
        Forest first = new Forest();
        harness.setLibrary(player1, List.of(first));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());

        castQualifyingSpellAndChoose(true);
        castQualifyingSpellAndChoose(true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.castFromExile(player1, first.getId());
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void exiledCreatureCanBeCastByPayingItsNormalCost() {
        GrizzlyBears exiled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(exiled));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        castQualifyingSpellAndChoose(true);

        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(exiled);
    }

    @Test
    void opponentsQualifyingSpellDoesNotTrigger() {
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }

    private void castQualifyingSpellAndChoose(boolean accepted) {
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, accepted);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    @Test
    void chosenXCountsTowardTheCastSpellsManaValue() {
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 3, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
    }

    @Test
    void separateCopiesKeepSeparatePlayPermissions() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.addToBattlefield(player1, new SuperiorFoesOfSpiderMan());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    void removingTheSourceDoesNotEndPlayPermission() {
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        var source = harness.addToBattlefieldAndReturn(player1, new SuperiorFoesOfSpiderMan());
        castQualifyingSpellAndChoose(true);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.castFromExile(player1, top.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(top);
    }
}
