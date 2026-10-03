package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.s.SwordOfWarAndPeace;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.s.Sickleslicer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokSurvivors.class, SwordOfWarAndPeace.class, PorcelainLegionnaire.class, Sickleslicer.class})
class AuriokSurvivorsTest extends BaseCardTest {

    private void castSurvivors() {
        harness.setHand(player1, List.of(new AuriokSurvivors()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTarget(int index) {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).get(index).getId()));
    }

    private void castAndAcceptMay() {
        castAndAcceptMay(0);
    }

    private void castAndAcceptMay(int index) {
        castSurvivors();
        chooseTarget(index);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Resolving Auriok Survivors triggers may ability prompt")
    void resolvingTriggersMayPrompt() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));

        castSurvivors();
        chooseTarget(0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting return offers optional attachment")
    void acceptingReturnOffersAttachment() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));
        castAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining may ability does not return Equipment")
    void decliningMaySkipsAbility() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));

        castSurvivors();
        chooseTarget(0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Auriok Survivors");
        harness.assertInGraveyard(player1, "Sword of War and Peace");
    }

    @Test
    @DisplayName("Returns Equipment from graveyard to battlefield and attaches to Auriok Survivors")
    void returnsEquipmentAndAttaches() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));
        castAndAcceptMay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sword of War and Peace");
        harness.assertNotInGraveyard(player1, "Sword of War and Peace");

        Permanent swordPerm = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Sword of War and Peace"));
        assertThat(swordPerm).isNotNull();
        assertThat(swordPerm.getAttachedTo()).isEqualTo(harness.getPermanentId(player1, "Auriok Survivors"));
    }

    @Test
    @DisplayName("Player can decline attachment and leave Equipment unattached")
    void decliningAttachmentLeavesEquipmentUnattached() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));
        castAndAcceptMay();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Sword of War and Peace");
        Permanent swordPerm = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Sword of War and Peace"));
        assertThat(swordPerm).isNotNull();
        assertThat(swordPerm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Choosing specific Equipment when multiple are in graveyard")
    void choosesSpecificEquipmentFromGraveyard() {
        harness.setGraveyard(player1, List.of(new Sickleslicer(), new SwordOfWarAndPeace()));
        castAndAcceptMay(1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sword of War and Peace");
        harness.assertInGraveyard(player1, "Sickleslicer");

        Permanent swordPerm = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Sword of War and Peace"));
        assertThat(swordPerm).isNotNull();
        assertThat(swordPerm.getAttachedTo()).isEqualTo(harness.getPermanentId(player1, "Auriok Survivors"));
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard has no Equipment")
    void noEffectWithNoEquipmentInGraveyard() {
        harness.setGraveyard(player1, List.of(new PorcelainLegionnaire()));
        castSurvivors();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Porcelain Legionnaire");
    }

    @Test
    @DisplayName("ETB resolves with no effect if graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        castSurvivors();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only Equipment cards are valid choices")
    void onlyEquipmentCardsAreValid() {
        harness.setGraveyard(player1, List.of(new PorcelainLegionnaire(), new SwordOfWarAndPeace()));
        castSurvivors();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipment returns to battlefield without attachment prompt if Auriok Survivors leaves")
    void equipmentReturnedWithoutAttachIfSourceGone() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));
        castSurvivors();
        chooseTarget(0);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Auriok Survivors"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Sword of War and Peace");
        Permanent swordPerm = harness.getGameQueryService().findPermanentById(
                gd, harness.getPermanentId(player1, "Sword of War and Peace"));
        assertThat(swordPerm).isNotNull();
        assertThat(swordPerm.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Stack is empty after full resolution with attachment")
    void stackIsEmptyAfterFullResolution() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));
        castAndAcceptMay();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Auriok Survivors remains on battlefield after returning Equipment")
    void survivorsRemainsOnBattlefield() {
        harness.setGraveyard(player1, List.of(new SwordOfWarAndPeace()));
        castAndAcceptMay();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Auriok Survivors");
    }

    @Test
    void targetIsChosenBeforeOpponentsCanRespond() {
        SwordOfWarAndPeace sword = new SwordOfWarAndPeace();
        harness.setGraveyard(player1, List.of(sword));
        castSurvivors();
        chooseTarget(0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Sword of War and Peace");
    }

    @Test
    void removedTargetDoesNotAllowReturningAnotherEquipment() {
        SwordOfWarAndPeace sword = new SwordOfWarAndPeace();
        Sickleslicer other = new Sickleslicer();
        harness.setGraveyard(player1, List.of(sword, other));
        castSurvivors();
        chooseTarget(0);
        gd.playerGraveyards.get(player1.getId()).remove(sword);
        gd.getPlayerExiledCards(player1.getId()).add(sword);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sickleslicer");
        harness.assertNotOnBattlefield(player1, "Sickleslicer");
    }

    @Test
    void cannotTargetEquipmentInOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new SwordOfWarAndPeace()));
        castSurvivors();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Sword of War and Peace");
    }
}
