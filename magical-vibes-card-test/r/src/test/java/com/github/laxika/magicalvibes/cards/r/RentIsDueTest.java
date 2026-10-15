package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MysteriosPhantasm;
import com.github.laxika.magicalvibes.cards.p.PeterParkersCamera;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RentIsDue.class, MysteriosPhantasm.class, PeterParkersCamera.class})
class RentIsDueTest extends BaseCardTest {

    @Test
    void tapsCreatureAndTreasureToDraw() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new MysteriosPhantasm());
        Permanent treasure = addTreasureToken(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MysteriosPhantasm()));

        resolveEndStepTrigger(true);

        assertThat(creature.isTapped()).isTrue();
        assertThat(treasure.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Rent Is Due");
    }

    @Test
    void sacrificesWhenTwoEligiblePermanentsCannotBeTapped() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new MysteriosPhantasm());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PeterParkersCamera());

        resolveEndStepTrigger(true);

        harness.assertInGraveyard(player1, "Rent Is Due");
        assertThat(creature.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void decliningToTapSacrificesRentIsDue() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new MysteriosPhantasm());
        addTreasureToken(player1);

        resolveEndStepTrigger(false);

        harness.assertInGraveyard(player1, "Rent Is Due");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void twoSummoningSickCreaturesCanPay() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MysteriosPhantasm());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MysteriosPhantasm());
        first.setSummoningSick(true);
        second.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MysteriosPhantasm()));

        resolveEndStepTrigger(true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Rent Is Due");
    }

    @Test
    void twoTreasuresCanPayWithoutBeingSacrificed() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent first = addTreasureToken(player1);
        Permanent second = addTreasureToken(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MysteriosPhantasm()));

        resolveEndStepTrigger(true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first, second);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Rent Is Due");
    }

    @Test
    void tappedPermanentsAndOpponentsPermanentsCannotPay() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent creature = addCreatureReady(player1, new MysteriosPhantasm());
        Permanent treasure = addTreasureToken(player1);
        treasure.tap();
        Permanent opposingCreature = addCreatureReady(player2, new MysteriosPhantasm());
        Permanent opposingTreasure = addTreasureToken(player2);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MysteriosPhantasm()));

        resolveEndStepTrigger(true);

        harness.assertInGraveyard(player1, "Rent Is Due");
        assertThat(creature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isFalse();
        assertThat(opposingTreasure.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void controllerChoosesExactlyTwoWhenMoreAreAvailable() {
        harness.addToBattlefield(player1, new RentIsDue());
        Permanent first = addCreatureReady(player1, new MysteriosPhantasm());
        Permanent second = addCreatureReady(player1, new MysteriosPhantasm());
        Permanent unused = addTreasureToken(player1);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new MysteriosPhantasm()));

        resolveEndStepTrigger(true);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(unused.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Rent Is Due");
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new RentIsDue());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Rent Is Due");
    }

    private void resolveEndStepTrigger(boolean accept) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, accept);
    }

    private Permanent addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.setToken(true);

        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
        return treasure;
    }

}
