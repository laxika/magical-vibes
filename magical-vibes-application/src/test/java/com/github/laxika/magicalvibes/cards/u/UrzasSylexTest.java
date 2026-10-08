package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.l.LoranOfTheThirdPath;
import com.github.laxika.magicalvibes.cards.y.YotianFrontliner;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.s.SoulPartition;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasSylex.class, Plains.class, YotianFrontliner.class, TeferiTemporalPilgrim.class, SoulPartition.class, LoranOfTheThirdPath.class})
class UrzasSylexTest extends BaseCardTest {

    @Test
    @DisplayName("The exile trigger resolves before each player chooses lands and destroys the rest")
    void searchResolvesBeforeLandChoicesAndBoardWipe() {
        UrzasSylex sylex = new UrzasSylex();
        harness.addToBattlefield(player1, sylex);
        List<Permanent> player1Lands = addPlains(player1, 7);
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        List<Permanent> player2Lands = addPlains(player2, 7);
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new YotianFrontliner());
        TeferiTemporalPilgrim teferi = new TeferiTemporalPilgrim();
        harness.setLibrary(player1, List.of(teferi, new YotianFrontliner()));

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(teferi);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex);
        assertThat(gd.playerHands.get(player1.getId())).contains(teferi);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(player2Creature);

        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.maxCount()).isEqualTo(6);
        assertThat(firstChoice.validIds()).containsExactlyElementsOf(ids(player1Lands));
        harness.handleMultiplePermanentsChosen(player1, ids(player1Lands.subList(0, 6)));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactlyElementsOf(ids(player2Lands));
        harness.handleMultiplePermanentsChosen(player2, ids(player2Lands.subList(0, 6)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player1Lands.subList(0, 6)));
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player2Lands.subList(0, 6)));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Creature.getCard());
    }

    @Test
    @DisplayName("Declining the search still destroys nonland permanents and keeps all lands")
    void decliningSearchStillDestroysOtherPermanents() {
        UrzasSylex sylex = new UrzasSylex();
        harness.addToBattlefield(player1, sylex);
        List<Permanent> player1Lands = addPlains(player1, 2);
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new YotianFrontliner());
        List<Permanent> player2Lands = addPlains(player2, 2);
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new YotianFrontliner());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player1Lands));
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(player2Lands));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(player1Creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(player2Creature.getCard());
    }

    @Test
    void exactlySixLandsAreKeptWhilePlayerWithoutLandsLosesTheirPermanents() {
        harness.addToBattlefield(player1, new UrzasSylex());
        List<Permanent> lands = addPlains(player1, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YotianFrontliner());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(ids(lands));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(creature.getCard(), planeswalker.getCard());
    }

    @Test
    void exileByAnotherSpellTriggersSearchWithoutDestroyingPermanents() {
        UrzasSylex sylex = new UrzasSylex();
        Permanent source = harness.addToBattlefieldAndReturn(player1, sylex);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new YotianFrontliner());
        TeferiTemporalPilgrim planeswalker = new TeferiTemporalPilgrim();
        harness.setLibrary(player1, List.of(planeswalker, new Plains()));
        harness.setHand(player1, List.of(new SoulPartition()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex);
        assertThat(gd.playerHands.get(player1.getId())).contains(planeswalker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayFailToFindEvenWhenLibraryContainsAPlaneswalker() {
        harness.addToBattlefield(player1, new UrzasSylex());
        TeferiTemporalPilgrim planeswalker = new TeferiTemporalPilgrim();
        harness.setLibrary(player1, List.of(planeswalker, new Plains()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(planeswalker);
        assertThat(gd.playerDecks.get(player1.getId())).contains(planeswalker);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        harness.addToBattlefield(player1, new UrzasSylex());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateWithSpellOnStack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UrzasSylex());
        harness.setHand(player1, List.of(new SoulPartition()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, source.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void tappedSylexCannotActivate() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UrzasSylex());
        source.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void activationRequiresTwoWhiteMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new UrzasSylex());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
    @Test
    void destructionDoesNotTriggerThePlaneswalkerSearch() {
        UrzasSylex sylex = new UrzasSylex();
        Permanent source = harness.addToBattlefieldAndReturn(player1, sylex);
        TeferiTemporalPilgrim planeswalker = new TeferiTemporalPilgrim();
        harness.setLibrary(player1, List.of(planeswalker, new Plains()));
        harness.setHand(player1, List.of(new LoranOfTheThirdPath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, List.of(source.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sylex);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(planeswalker);
    }
    private List<Permanent> addPlains(com.github.laxika.magicalvibes.model.Player player, int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lands.add(harness.addToBattlefieldAndReturn(player, new Plains()));
        }
        return lands;
    }

    private List<UUID> ids(List<Permanent> permanents) {
        return permanents.stream().map(Permanent::getId).toList();
    }
}
