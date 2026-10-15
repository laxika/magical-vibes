package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdelbertSteiner;
import com.github.laxika.magicalvibes.cards.a.AerithGainsborough;
import com.github.laxika.magicalvibes.cards.b.BarretWallace;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SazhsChocobo;
import com.github.laxika.magicalvibes.cards.t.TifaLockhart;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClivesHideaway.class, SazhsChocobo.class, AdelbertSteiner.class,
        AerithGainsborough.class, BarretWallace.class, Plains.class, TifaLockhart.class})
class ClivesHideawayTest extends BaseCardTest {

    private Permanent addHideawayWithImprint(Card imprinted) {
        Permanent hideaway = harness.addToBattlefieldAndReturn(player1, new ClivesHideaway());
        gd.setImprintedCard(hideaway.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted);
        return hideaway;
    }

    @Test
    @DisplayName("Tapping adds colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new ClivesHideaway());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Plays the exiled card when four legendary creatures are controlled")
    void playsExiledCardWithFourLegendaryCreatures() {
        addHideawayWithImprint(new SazhsChocobo());
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new TifaLockhart());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sazh's Chocobo");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Sazh's Chocobo"));
    }

    @Test
    @DisplayName("Does nothing with fewer than four legendary creatures")
    void doesNothingBelowLegendaryCreatureThreshold() {
        addHideawayWithImprint(new Plains());
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new SazhsChocobo());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        addHideawayWithImprint(new SazhsChocobo());
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new TifaLockhart());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Sazh's Chocobo"));
        harness.assertNotOnBattlefield(player1, "Sazh's Chocobo");
    }

    private void addFourLegendaryCreatures() {
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player1, new TifaLockhart());
    }

    @Test
    void hideawayExilesOneOfTopFourAndRandomizesTheRestWithoutAnOrderChoice() {
        Card pick = new AdelbertSteiner();
        Card second = new AerithGainsborough();
        Card third = new BarretWallace();
        Card fourth = new TifaLockhart();
        Card fifth = new Plains();
        harness.setLibrary(player1, List.of(pick, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new ClivesHideaway()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent hideaway = findPermanent(player1, "Clive's Hideaway");
        assertThat(gd.findExiledCard(pick.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(hideaway.getCard())).isSameAs(pick);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(second, third, fourth);
    }

    @Test
    void hideawayWithOneCardExilesItWithoutAChoice() {
        Card pick = new Plains();
        harness.setLibrary(player1, List.of(pick));
        harness.setHand(player1, List.of(new ClivesHideaway()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(pick.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(findPermanent(player1, "Clive's Hideaway").getCard()))
                .isSameAs(pick);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canPlayExiledLandDuringOwnTurnWithAnUnusedLandPlay() {
        Card land = new Plains();
        addHideawayWithImprint(land);
        addFourLegendaryCreatures();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayExiledLandAfterUsingAllLandPlays() {
        Card land = new Plains();
        Permanent hideaway = addHideawayWithImprint(land);
        addFourLegendaryCreatures();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Plains()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.getImprintedCard(hideaway.getCard())).isSameAs(land);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(land.getId()));
    }

    @Test
    void cannotPlayExiledLandDuringOpponentsTurn() {
        Card land = new Plains();
        Permanent hideaway = addHideawayWithImprint(land);
        addFourLegendaryCreatures();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        assertThat(gd.getImprintedCard(hideaway.getCard())).isSameAs(land);
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    void checksLegendaryCreatureCountWhenAbilityResolves() {
        Card land = new Plains();
        addHideawayWithImprint(land);
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new TifaLockhart());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void opponentsLegendaryCreaturesDoNotCount() {
        Card land = new Plains();
        addHideawayWithImprint(land);
        harness.addToBattlefield(player1, new AdelbertSteiner());
        harness.addToBattlefield(player1, new AerithGainsborough());
        harness.addToBattlefield(player1, new BarretWallace());
        harness.addToBattlefield(player2, new TifaLockhart());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.findExiledCard(land.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Plains");
    }

    @Test
    void canCastExiledCreatureDuringOpponentsTurnWithoutPayingMana() {
        Card creature = new SazhsChocobo();
        addHideawayWithImprint(creature);
        addFourLegendaryCreatures();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sazh's Chocobo");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void hideawayWithEmptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ClivesHideaway()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getImprintedCard(findPermanent(player1, "Clive's Hideaway").getCard()))
                .isNull();
    }
}
