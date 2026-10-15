package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeedStrangle.class, Forest.class, KnightOfMeadowgrain.class})
class WeedStrangleTest extends BaseCardTest {

    private Permanent prepareTargetCreature() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new KnightOfMeadowgrain());
        harness.setHand(player1, List.of(new WeedStrangle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        return creature;
    }

    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new KnightOfMeadowgrain(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    private void finishClash() {
        for (int i = 0; i < 2; i++) {
            PendingInteraction.Scry choice = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
            if (choice == null) {
                return;
            }
            gs.handleInteractionAnswer(gd, choice.playerId().equals(player1.getId()) ? player1 : player2,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Winning the clash destroys the creature and gains life equal to its toughness")
    void wonClashDestroysAndGainsLife() {
        Permanent creature = prepareTargetCreature();
        stackClashWinForCaster();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertNotOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertInGraveyard(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Losing the clash still destroys the creature but gains no life")
    void lostClashDestroysButGainsNoLife() {
        Permanent creature = prepareTargetCreature();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new KnightOfMeadowgrain(), new Forest(), new Forest()));
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertInGraveyard(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tied clash destroys the creature but gains no life")
    void tiedClashGainsNoLife() {
        Permanent creature = prepareTargetCreature();
        harness.setLibrary(player1, List.of(new KnightOfMeadowgrain(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new KnightOfMeadowgrain(), new Forest(), new Forest()));
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertInGraveyard(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepareTargetCreature();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Winning uses the destroyed creature's modified toughness")
    void gainsLifeForModifiedToughness() {
        Permanent creature = prepareTargetCreature();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        stackClashWinForCaster();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertInGraveyard(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("An indestructible target survives but a clash win still gains life")
    void indestructibleTargetStillGrantsLife() {
        Permanent creature = prepareTargetCreature();
        creature.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        stackClashWinForCaster();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertNotInGraveyard(player2, "Knight of Meadowgrain");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A regenerated target survives but a clash win still gains life")
    void regeneratedTargetStillGrantsLife() {
        Permanent creature = prepareTargetCreature();
        creature.setRegenerationShield(1);
        stackClashWinForCaster();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertNotInGraveyard(player2, "Knight of Meadowgrain");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("An illegal target prevents the clash and life gain")
    void illegalTargetPreventsClash() {
        Permanent creature = prepareTargetCreature();
        stackClashWinForCaster();
        harness.castSorcery(player1, 0, creature.getId());
        creature.getGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertInGraveyard(player1, "Weed Strangle");
        harness.assertLife(player1, 20);
        assertThat(gd.lastClashWonByController).doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("The creature is destroyed before either player places their revealed card")
    void destroysBeforeClashPlacementChoices() {
        Permanent creature = prepareTargetCreature();
        stackClashWinForCaster();
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        boolean destroyedBeforePlacement = gd.playerGraveyards.get(player2.getId()).contains(creature.getCard());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        finishClash();
        assertThat(destroyedBeforePlacement).isTrue();
        harness.assertLife(player1, 22);
    }

    @Test
    @CardUsed({WheelOfSunAndMoon.class})
    @DisplayName("Destruction's library replacement changes the card revealed for the clash")
    void destructionReplacementChangesClashOutcome() {
        Permanent creature = prepareTargetCreature();
        Permanent wheel = harness.addToBattlefieldAndReturn(player1, new WheelOfSunAndMoon());
        wheel.setAttachedTo(player2.getId());
        harness.setLibrary(player1, List.of(new KnightOfMeadowgrain(), new Forest()));
        harness.setLibrary(player2, List.of());
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        finishClash();
        harness.assertNotOnBattlefield(player2, "Knight of Meadowgrain");
        harness.assertNotInGraveyard(player2, "Knight of Meadowgrain");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature.getCard());
        harness.assertLife(player1, 20);
    }
}
