package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.m.MazemindTome;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GadrakTheCrownScourge.class, AlpineWatchdog.class, MazemindTome.class, Shock.class, Card.class})
class GadrakTheCrownScourgeTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack unless controller controls four artifacts")
    void cannotAttackWithoutFourArtifacts() {
        addCreatureReady(player1, new GadrakTheCrownScourge());
        addArtifacts(player1, 3);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controller controls four artifacts")
    void canAttackWithFourArtifacts() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GadrakTheCrownScourge());
        addArtifacts(player1, 4);

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Creates one Treasure for each nontoken creature that died this turn")
    void createsTreasureForNontokenCreatureDeaths() {
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());
        Permanent bears = addCreatureReady(player1, new AlpineWatchdog());
        Permanent token = addCreatureReady(player1, createTokenCreature());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void opponentsArtifactsDoNotAllowAttacking() {
        addCreatureReady(player1, new GadrakTheCrownScourge());
        addArtifacts(player1, 3);
        addArtifacts(player2, 4);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsBothPlayersNontokenCreatureDeaths() {
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());
        Permanent friendly = addCreatureReady(player1, new AlpineWatchdog());
        Permanent opposing = addCreatureReady(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, friendly.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, opposing.getId());
        harness.passBothPriorities();

        advanceToEndStep();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    void countsDeathsBeforeGadrakEnteredTheBattlefield() {
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void createsNoTreasureWhenNoCreaturesDied() {
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());

        advanceToEndStep();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void countsDeathsWhileEndStepTriggerIsOnStack() {
        harness.addToBattlefield(player1, new GadrakTheCrownScourge());
        Permanent creature = addCreatureReady(player1, new AlpineWatchdog());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private void addArtifacts(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new MazemindTome());
        }
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    private Card createTokenCreature() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
