package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.o.OtepecHuntmaster;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XavierSalInfestedCaptain.class, OtepecHuntmaster.class, SolRing.class})
class XavierSalInfestedCaptainTest extends BaseCardTest {

    @Test
    void removesCounterAndPopulates() {
        addCaptain();
        Permanent token = addCreatureReady(player1, creatureToken());
        token.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        activate(0);
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    void sacrificesAnotherCreatureAndProliferates() {
        addCaptain();
        addCreatureReady(player1, new OtepecHuntmaster());
        Permanent target = addCreatureReady(player2, new OtepecHuntmaster());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        activate(1);
        harness.assertInGraveyard(player1, "Otepec Huntmaster");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotPayEitherAbilityWithOnlyTheCaptain() {
        addCaptain();

        assertThatThrownBy(() -> activate(0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> activate(1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotRemoveACounterFromTheCaptainItself() {
        Permanent captain = addCaptain();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> activate(0)).isInstanceOf(IllegalStateException.class);

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canRemoveACounterFromANoncreaturePermanent() {
        addCaptain();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        artifact.setCounterCount(CounterType.CHARGE, 1);
        addCreatureReady(player1, creatureToken());

        activate(0);
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(countPermanents(player1, "Soldier Token")).isEqualTo(2);
    }

    @Test
    void cannotSacrificeANoncreaturePermanent() {
        addCaptain();
        harness.addToBattlefield(player1, new SolRing());

        assertThatThrownBy(() -> activate(1)).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void cannotPayCostsWithOpponentsPermanents() {
        addCaptain();
        Permanent opponentCreature = addCreatureReady(player2, new OtepecHuntmaster());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> activate(0)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> activate(1)).isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Otepec Huntmaster");
    }

    @Test
    void populatePaysCostEvenWithoutACreatureToken() {
        addCaptain();
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player2, creatureToken());

        activate(0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Soldier Token")).isZero();
        assertThat(countPermanents(player2, "Soldier Token")).isEqualTo(1);
    }

    @Test
    void populateDoesNotCopyCountersOrTappedState() {
        addCaptain();
        Permanent token = addCreatureReady(player1, creatureToken());
        token.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        token.tap();

        activate(0);
        harness.passBothPriorities();

        Permanent copy = findPermanents(player1, "Soldier Token").stream()
                .filter(permanent -> !permanent.getId().equals(token.getId()))
                .findFirst().orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
    }

    @Test
    void removingACounterRequiresChoosingItsKindWhenSeveralKindsArePresent() {
        addCaptain();
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.setCounterCount(CounterType.FINALITY, 1);

        activate(0);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }

    @Test
    void proliferateAddsEveryExistingKindToSelectedPermanentsAndPlayers() {
        Permanent captain = addCaptain();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        captain.setCounterCount(CounterType.FINALITY, 1);
        addCreatureReady(player1, new OtepecHuntmaster());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player2.getId(), 2);

        activate(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(captain.getId(), player2.getId()));

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(captain.getCounterCount(CounterType.FINALITY)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(3);
    }

    @Test
    void proliferateMayChooseNothing() {
        Permanent captain = addCaptain();
        captain.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new OtepecHuntmaster());

        activate(1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Otepec Huntmaster");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotActivateOutsideMainPhase(int abilityIndex) {
        addCaptain();
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotActivateDuringOpponentsMainPhase(int abilityIndex) {
        addCaptain();
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotActivateWhileTapped(int abilityIndex) {
        Permanent captain = addCaptain();
        captain.tap();
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> activate(abilityIndex)).isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotActivateWhileSummoningSick(int abilityIndex) {
        Permanent captain = addCaptain();
        captain.setSummoningSick(true);
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> activate(abilityIndex)).isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void cannotActivateWithAnAbilityOnTheStack(int abilityIndex) {
        Permanent captain = addCaptain();
        Permanent creature = addCreatureReady(player1, new OtepecHuntmaster());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        activate(0);
        captain.untap();

        assertThatThrownBy(() -> activate(abilityIndex)).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCaptain() {
        return addCreatureReady(player1, new XavierSalInfestedCaptain());
    }

    private void activate(int abilityIndex) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, abilityIndex, null, null);
    }

    private static Card creatureToken() {
        Card card = new Card();
        card.setName("Soldier Token");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
