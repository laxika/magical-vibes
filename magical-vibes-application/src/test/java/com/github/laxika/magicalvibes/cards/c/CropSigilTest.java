package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CropSigil.class, Divination.class, Forest.class, GrizzlyBears.class, HolyDay.class, Ornithopter.class})
class CropSigilTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one creature and up to one land from the graveyard")
    void returnsCreatureAndLand() {
        Permanent cropSigil = addCropSigil();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, deliriumGraveyard(creature, land));
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0,
                List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Crop Sigil");
    }

    @Test
    @DisplayName("May activate and sacrifice itself without choosing targets")
    void allowsNoTargets() {
        Permanent cropSigil = addCropSigil();
        harness.setGraveyard(player1, deliriumGraveyard());
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crop Sigil");
    }

    @Test
    @DisplayName("Rejects two creature targets")
    void rejectsTwoCreatureTargets() {
        Permanent cropSigil = addCropSigil();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, deliriumGraveyard(firstCreature, secondCreature));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature");
    }

    @Test
    @DisplayName("Requires delirium to activate")
    void requiresDelirium() {
        Permanent cropSigil = addCropSigil();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new HolyDay()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more card types");
    }

    @Test
    @DisplayName("May mill a card during upkeep")
    void mayMillDuringUpkeep() {
        addCropSigil();
        Card milled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milled));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineUpkeepMill() {
        addCropSigil();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void mayMillFromEmptyLibrary() {
        addCropSigil();
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Crop Sigil");
    }

    @Test
    void sacrificingSigilCannotSupplyFourthTypeForActivation() {
        Permanent cropSigil = addCropSigil();
        harness.setGraveyard(player1, List.of(new HolyDay(), new Ornithopter()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more card types");
        harness.assertOnBattlefield(player1, "Crop Sigil");
        harness.assertNotInGraveyard(player1, "Crop Sigil");
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        addCropSigil();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void returnsOnlyChosenCreature() {
        Permanent cropSigil = addCropSigil();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, deliriumGraveyard(creature, land));
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0,
                List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Crop Sigil");
    }

    @Test
    void returnsOnlyChosenLand() {
        Permanent cropSigil = addCropSigil();
        Card land = new Forest();
        harness.setGraveyard(player1, deliriumGraveyard(land));
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0,
                List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Ornithopter");
    }

    @Test
    void rejectsTwoLandTargets() {
        Permanent cropSigil = addCropSigil();
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setGraveyard(player1, deliriumGraveyard(firstLand, secondLand));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of(firstLand.getId(), secondLand.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Crop Sigil");
    }

    @Test
    void rejectsOpponentsGraveyardCard() {
        Permanent cropSigil = addCropSigil();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, deliriumGraveyard());
        harness.setGraveyard(player2, List.of(opposingCreature));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Crop Sigil");
    }

    @Test
    void rejectsNoncreatureNonlandTarget() {
        Permanent cropSigil = addCropSigil();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, deliriumGraveyard(instant));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Crop Sigil");
    }

    @Test
    void returnsRemainingTargetEvenAfterLosingDelirium() {
        Permanent cropSigil = addCropSigil();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, deliriumGraveyard(creature, land));
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0,
                List.of(creature.getId(), land.getId()));
        // Model graveyard removal while the activated ability is on the stack.
        harness.setGraveyard(player1, List.of(land, cropSigil.getCard()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Crop Sigil");
    }

    private Permanent addCropSigil() {
        return harness.addToBattlefieldAndReturn(player1, new CropSigil());
    }

    private int cropSigilIndex(Permanent cropSigil) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(cropSigil);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> deliriumGraveyard(Card... additionalCards) {
        List<Card> cards = new ArrayList<>(List.of(
                new HolyDay(), new Divination(), new Ornithopter()));
        cards.addAll(List.of(additionalCards));
        return cards;
    }
}
