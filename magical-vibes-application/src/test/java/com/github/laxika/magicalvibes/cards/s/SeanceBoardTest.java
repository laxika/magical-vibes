package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DemonOfFatesDesign;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeanceBoard.class, GrizzlyBears.class, LightningBolt.class, Shock.class, PropheticPrism.class,
        SignInBlood.class, DemonOfFatesDesign.class, SpectralRider.class})
class SeanceBoardTest extends BaseCardTest {

    @Test
    void morbidPutsASoulCounterOnTheBoardAtEndStep() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(board.getCounterCount(CounterType.SOUL)).isEqualTo(1);
    }

    @Test
    void activatedAbilityAddsRestrictedManaAndPaysForInstant() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        Set<CardSubtype> allowedSubtypes = Set.of(CardSubtype.DEMON, CardSubtype.SPIRIT);
        assertThat(pool.getInstantSorceryOrSubtypeSpellOnlyManaForColor(allowedSubtypes, ManaColor.RED))
                .isEqualTo(2);

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(pool.getInstantSorceryOrSubtypeSpellOnlyManaForColor(allowedSubtypes, ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void restrictedManaCannotPayForOtherSpells() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.setHand(player1, List.of(new PropheticPrism()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void noSoulCounterWhenNoCreatureDied() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(board.getCounterCount(CounterType.SOUL)).isZero();
    }

    @Test
    void morbidTriggersOnOpponentsEndStepAndOnlyAddsOneCounterForMultipleDeaths() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, first.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, second.getId());
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(board.getCounterCount(CounterType.SOUL)).isEqualTo(1);
    }

    @Test
    void deathAfterEndStepBeginsDoesNotTriggerMorbidRetroactively() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(board.getCounterCount(CounterType.SOUL)).isZero();
    }

    @Test
    void zeroSoulCountersProducesNoManaButStillTaps() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        harness.activateAbility(player1, 0, null, null);

        assertThat(board.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getInstantSorceryOrSubtypeSpellOnlyManaTotal(
                Set.of(CardSubtype.DEMON, CardSubtype.SPIRIT))).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void restrictedManaPaysForSorcery() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new SeanceBoard(), new SeanceBoard()));
        harness.setHand(player1, List.of(new SignInBlood()));
        harness.castSorcery(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void restrictedManaPaysForDemonIncludingGenericCost() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.setHand(player1, List.of(new DemonOfFatesDesign()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Demon of Fate's Design");
        assertThat(board.getCounterCount(CounterType.SOUL)).isEqualTo(6);
    }

    @Test
    void restrictedManaPaysForSpirit() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());
        harness.setHand(player1, List.of(new SpectralRider()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Spectral Rider");
    }

    @Test
    void restrictedManaCannotPayForCreatureWithoutAllowedSubtype() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        board.setCounterCount(CounterType.SOUL, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictedManaCannotPayForActivatedAbility() {
        Permanent board = addCreatureReady(player1, new SeanceBoard());
        addCreatureReady(player1, new PropheticPrism());
        board.setCounterCount(CounterType.SOUL, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
