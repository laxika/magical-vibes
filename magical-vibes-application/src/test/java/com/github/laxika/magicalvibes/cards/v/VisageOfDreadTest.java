package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArmoredKincaller;
import com.github.laxika.magicalvibes.cards.d.DreadOsseosaur;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlowcapLantern;
import com.github.laxika.magicalvibes.cards.z.ZoeticGlyph;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VisageOfDread.class, DreadOsseosaur.class, GlowcapLantern.class, Forest.class,
        ArmoredKincaller.class, ZoeticGlyph.class})
class VisageOfDreadTest extends BaseCardTest {

    @Test
    void entersAndDiscardsAChosenArtifactOrCreature() {
        Card artifact = new GlowcapLantern();
        Card creature = new ArmoredKincaller();
        Card land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(artifact, creature, land)));
        harness.setHand(player1, List.of(new VisageOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(artifact, land);
    }

    @Test
    void craftWithTwoCreaturesReturnsDreadOsseosaurAndMayMill() {
        harness.addToBattlefieldAndReturn(player1, new VisageOfDread());
        Permanent battlefieldCreature = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Card graveyardCreature = new ArmoredKincaller();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isTransformed()
                        && permanent.getCard() instanceof DreadOsseosaur);
        assertThat(gd.findExiledCard(battlefieldCreature.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(graveyardCreature.getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest).hasSize(2);
    }

    @Test
    void attackingMayMillTwoCards() {
        addCreatureReady(player1, new DreadOsseosaur());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Forest).hasSize(2);
    }

    @Test
    void choosesAnArtifactToDiscard() {
        Card artifact = new GlowcapLantern();
        Card creature = new ArmoredKincaller();
        harness.setHand(player2, List.of(artifact, creature));
        castVisage();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void handWithoutArtifactsOrCreaturesDiscardsNothing() {
        Card land = new Forest();
        Card enchantment = new ZoeticGlyph();
        harness.setHand(player2, List.of(land, enchantment));
        castVisage();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land, enchantment);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyHandDoesNotRequireAChoice() {
        harness.setHand(player2, List.of());
        castVisage();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Visage of Dread");
    }

    @Test
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new VisageOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void craftCanUseTwoGraveyardCreaturesAndDeclineEntryMill() {
        Permanent visage = harness.addToBattlefieldAndReturn(player1, new VisageOfDread());
        Card first = new ArmoredKincaller();
        Card second = new ArmoredKincaller();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(libraryCard));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(visage);
        assertThat(gd.findExiledCard(visage.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Dread Osseosaur");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void craftChoosesExactlyTwoBattlefieldCreatures() {
        harness.addToBattlefield(player1, new VisageOfDread());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new ArmoredKincaller());
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CraftMaterialChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getCard().getId(), second.getCard().getId()));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(first.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen);
        harness.assertOnBattlefield(player1, "Dread Osseosaur");
    }

    @Test
    void craftCannotUseOpponentsCreaturesOrNoncreatureGraveyardCards() {
        harness.addToBattlefield(player1, new VisageOfDread());
        harness.addToBattlefield(player2, new ArmoredKincaller());
        harness.setGraveyard(player1, List.of(new ArmoredKincaller(), new GlowcapLantern()));
        addCraftMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Visage of Dread");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void craftCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new VisageOfDread());
        harness.setGraveyard(player1, List.of(new ArmoredKincaller(), new ArmoredKincaller()));
        addCraftMana();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Visage of Dread");
    }

    @Test
    void craftCanUseAnArtifactAnimatedByZoeticGlyph() {
        harness.addToBattlefield(player1, new VisageOfDread());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GlowcapLantern());
        harness.setGraveyard(player1, List.of(new ArmoredKincaller()));
        harness.setHand(player1, List.of(new ZoeticGlyph()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, artifact.getId());
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, artifact)).isTrue();
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.findExiledCard(artifact.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
    }

    @Test
    void craftReturnsUnderOwnersControlAndOwnerChoosesWhetherToMill() {
        Card visage = new VisageOfDread();
        visage.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, visage);
        harness.setGraveyard(player1, List.of(new ArmoredKincaller(), new ArmoredKincaller()));
        Card ownerLibraryCard = new Forest();
        Card controllerLibraryCard = new Forest();
        harness.setLibrary(player2, List.of(ownerLibraryCard));
        harness.setLibrary(player1, List.of(controllerLibraryCard));
        addCraftMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Dread Osseosaur");
        harness.assertNotOnBattlefield(player1, "Dread Osseosaur");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ownerLibraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerLibraryCard);
    }

    @Test
    void attackingCanDeclineMill() {
        addCreatureReady(player1, new DreadOsseosaur());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void attackingMillsTheRemainingCardOfAShortLibrary() {
        addCreatureReady(player1, new DreadOsseosaur());
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    void menaceRequiresTwoBlockersAndAllowsTwo() {
        addCreatureReady(player1, new DreadOsseosaur());
        Permanent first = addCreatureReady(player2, new ArmoredKincaller());
        Permanent second = addCreatureReady(player2, new ArmoredKincaller());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }

    private void castVisage() {
        harness.setHand(player1, List.of(new VisageOfDread()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0, player2.getId());
        resolveAllTriggers();
    }

    private void addCraftMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
