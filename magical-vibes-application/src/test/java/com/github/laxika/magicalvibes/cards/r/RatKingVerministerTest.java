package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RatKingVerminister.class, GrizzlyBears.class})
class RatKingVerministerTest extends BaseCardTest {

    @Test
    void disappearCreatesRatAndPutsCounterOnRatKing() {
        RatKingVerminister ratKing = new RatKingVerminister();
        harness.addToBattlefield(player1, ratKing);
        Permanent leaving = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, leaving));
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Rat")).hasSize(1);
        Permanent ratKingPermanent = findPermanent(player1, "Rat King, Verminister");
        assertThat(ratKingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void disappearDoesNothingWithoutControlledPermanentLeaving() {
        RatKingVerminister ratKing = new RatKingVerminister();
        harness.addToBattlefield(player1, ratKing);

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(findPermanent(player1, "Rat King, Verminister")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void returnsTargetCreatureAndAllCardsWithItsNameTapped() {
        Permanent ratKing = addReadyRatKing();
        addRatTokens(3);

        Card target = new GrizzlyBears();
        Card sameName = new GrizzlyBears();
        Card otherCreature = createCreature("Other Creature");
        harness.setGraveyard(player1, List.of(target, sameName, otherCreature));

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        for (Permanent rat : findPermanents(player1, "Rat")) {
            harness.handlePermanentChosen(player1, rat.getId());
        }
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(findPermanents(player1, "Rat")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"))
                .anyMatch(card -> card.getName().equals("Other Creature"));
        assertThat(ratKing.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreatureCardInGraveyard() {
        Permanent ratKing = addReadyRatKing();
        addRatTokens(3);
        Card nonCreature = new Card() {
        };
        nonCreature.setName("Noncreature");
        nonCreature.setType(CardType.ARTIFACT);
        harness.setGraveyard(player1, List.of(nonCreature));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, nonCreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ratKing.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Rat")).hasSize(3);
    }

    private Permanent addReadyRatKing() {
        Permanent ratKing = addCreatureReady(player1, new RatKingVerminister());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return ratKing;
    }

    private void addRatTokens(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, createRatToken());
        }
    }

    private Card createRatToken() {
        Card rat = new Card() {
        };
        rat.setName("Rat");
        rat.setType(CardType.CREATURE);
        rat.setColor(CardColor.BLACK);
        rat.setSubtypes(List.of(CardSubtype.RAT));
        rat.setPower(1);
        rat.setToughness(1);
        rat.setToken(true);
        return rat;
    }

    private Card createCreature(String name) {
        Card creature = new Card() {
        };
        creature.setName(name);
        creature.setType(CardType.CREATURE);
        creature.setPower(1);
        creature.setToughness(1);
        return creature;
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
