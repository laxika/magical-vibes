package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlindSpotGiant;
import com.github.laxika.magicalvibes.cards.l.LashOut;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindbriskHeights.class, BlindSpotGiant.class, Plains.class, LashOut.class})
class WindbriskHeightsTest extends BaseCardTest {

    /** Puts Windbrisk Heights on the battlefield with {@code imprinted} exiled/imprinted on it. */
    private Permanent addHeightsWithImprint(Card imprinted) {
        Permanent heights = harness.addToBattlefieldAndReturn(player1, new WindbriskHeights());
        gd.setImprintedCard(heights.getCard(), imprinted);
        gd.addToExile(player1.getId(), imprinted, heights.getId(), true);
        return heights;
    }

    @Test
    @DisplayName("Hideaway ETB exiles the chosen card face down and imprints it on the land")
    void hideawayEtbExilesChosenCardFaceDown() {
        BlindSpotGiant pick = new BlindSpotGiant();
        BlindSpotGiant other = new BlindSpotGiant();
        harness.setLibrary(player1, List.of(pick, other));
        harness.setHand(player1, List.of(new WindbriskHeights()));
        harness.forceActivePlayer(player1);

        harness.playLand(player1, 0);
        harness.passBothPriorities(); // resolve the hideaway ETB trigger -> library choice
        harness.handleCardChosen(player1, 0);

        Permanent heights = findPermanent(player1, "Windbrisk Heights");
        ExiledCardEntry entry = gd.findExiledCard(pick.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.getImprintedCard(heights.getCard())).isSameAs(pick);
        // The other looked-at card went to the bottom of the library
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Plays the exiled card when the controller attacked with three or more creatures this turn")
    void playsExiledCardAfterThreeAttackers() {
        BlindSpotGiant creature = new BlindSpotGiant();
        addHeightsWithImprint(creature);
        gd.creaturesAttackedWithThisTurn.put(player1.getId(),
                Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability -> offers "may play"
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the free-cast creature spell

        harness.assertOnBattlefield(player1, "Blind-Spot Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Blind-Spot Giant"));
    }

    @Test
    @DisplayName("Does nothing while fewer than three creatures attacked this turn")
    void doesNothingBelowThreshold() {
        BlindSpotGiant creature = new BlindSpotGiant();
        addHeightsWithImprint(creature);
        gd.creaturesAttackedWithThisTurn.put(player1.getId(), Set.of(UUID.randomUUID(), UUID.randomUUID()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the ability; condition not met

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blind-Spot Giant"));
        harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
    }

    @Test
    @DisplayName("An opponent's attackers do not satisfy the controller's condition")
    void opponentAttackersDoNotCount() {
        BlindSpotGiant creature = new BlindSpotGiant();
        addHeightsWithImprint(creature);
        gd.creaturesAttackedWithThisTurn.put(player2.getId(),
                Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blind-Spot Giant"));
        harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
    }

    @Test
    @DisplayName("Declining the may choice leaves the card exiled")
    void decliningLeavesCardExiled() {
        BlindSpotGiant creature = new BlindSpotGiant();
        addHeightsWithImprint(creature);
        gd.creaturesAttackedWithThisTurn.put(player1.getId(),
                Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blind-Spot Giant"));
        harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
    }

    @Test
    void entersTapped() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new WindbriskHeights()));
        harness.playLand(player1, 0);
        assertThat(findPermanent(player1, "Windbrisk Heights").isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void tapsForWhiteManaWithoutAttackers() {
        Permanent heights = harness.addToBattlefieldAndReturn(player1, new WindbriskHeights());
        harness.tapPermanent(player1, 0);
        assertThat(heights.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hideawayLooksAtOnlyFourAndBottomsTheRest() {
        Card pick = new BlindSpotGiant();
        Card second = new Plains();
        Card third = new BlindSpotGiant();
        Card fourth = new Plains();
        Card fifth = new BlindSpotGiant();
        harness.setLibrary(player1, List.of(pick, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new WindbriskHeights()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.findExiledCard(pick.getId()).faceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(fifth);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .containsExactlyInAnyOrder(second, third, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void hideawayExilesTheOnlyLibraryCardAutomatically() {
        Card pick = new BlindSpotGiant();
        harness.setLibrary(player1, List.of(pick));
        harness.setHand(player1, List.of(new WindbriskHeights()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(pick.getId()).faceDown()).isTrue();
        assertThat(gd.getImprintedCard(findPermanent(player1, "Windbrisk Heights").getCard())).isSameAs(pick);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void playsHiddenLandDuringCombatWithAnAvailableLandPlay() {
        Plains plains = new Plains();
        prepareActivation(plains);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Plains");
        assertThat(gd.findExiledCard(plains.getId())).isNull();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotPlayHiddenLandAfterUsingTheLandPlay() {
        Plains plains = new Plains();
        prepareActivation(plains);
        gd.landsPlayedThisTurn.put(player1.getId(), 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        harness.assertNotOnBattlefield(player1, "Plains");
        assertThat(gd.findExiledCard(plains.getId())).isNotNull();
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void anUncastableHiddenSpellRemainsAvailableForAnotherActivation() {
        LashOut spell = new LashOut();
        Permanent heights = prepareActivation(spell);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();

        harness.addToBattlefield(player2, new BlindSpotGiant());
        heights.untap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    void totalAttackDeclarationsDoNotReplaceThreeDistinctAttackers() {
        prepareActivation(new BlindSpotGiant());
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 4);
        gd.creaturesAttackedWithThisTurn.put(player1.getId(), Set.of(UUID.randomUUID(), UUID.randomUUID()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
    }

    @Test
    void castsHiddenCreatureDuringCombatWithoutPayingItsManaCost() {
        BlindSpotGiant creature = new BlindSpotGiant();
        prepareActivation(creature);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Blind-Spot Giant");
        assertThat(gd.findExiledCard(creature.getId())).isNull();
    }

    @Test
    void doesNothingWhenTheHiddenCardHasLeftExile() {
        Card creature = new BlindSpotGiant();
        prepareActivation(creature);
        gd.removeFromExile(creature.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Blind-Spot Giant");
    }

    private Permanent prepareActivation(Card card) {
        Permanent heights = addHeightsWithImprint(card);
        gd.creaturesAttackedWithThisTurn.put(player1.getId(),
                Set.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        return heights;
    }
}
