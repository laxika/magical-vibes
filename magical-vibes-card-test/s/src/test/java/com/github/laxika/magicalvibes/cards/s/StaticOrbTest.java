package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HelmOfPossession;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.l.LotusPetal;
import com.github.laxika.magicalvibes.cards.m.MinimusContainment;
import com.github.laxika.magicalvibes.cards.w.WinterOrb;
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

@CardUsed({StaticOrb.class, LowlandGiant.class, Forest.class, LotusPetal.class,
        MinimusContainment.class, HelmOfPossession.class, WinterOrb.class})
class StaticOrbTest extends BaseCardTest {

    @Test
    @DisplayName("Only the two chosen permanents untap; the rest stay tapped")
    void picksTwoOfThreeToUntap() {
        addCreatureReady(player1, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.clearMessages();
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId(), forest.getId()));

        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(petal.isTapped()).isTrue();
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
    }

    @Test
    @DisplayName("A tapped Static Orb imposes no restriction — everything untaps normally")
    void tappedStaticOrbImposesNoRestriction() {
        Permanent orb = addCreatureReady(player1, new StaticOrb());
        orb.tap();
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);

        // No choice was presented; the untap step untapped everything, including the Orb.
        assertThat(orb.isTapped()).isFalse();
        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(petal.isTapped()).isFalse();
    }

    @Test
    @CardUsed(MinimusContainment.class)
    @DisplayName("A Static Orb that has lost its abilities imposes no restriction")
    void abilitylessStaticOrbImposesNoRestriction() {
        Permanent orb = addCreatureReady(player1, new StaticOrb());
        Permanent containment = harness.addToBattlefieldAndReturn(player2, new MinimusContainment());
        containment.setAttachedTo(orb.getId());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(petal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two or fewer permanents untap normally without a choice")
    void twoOrFewerUntapNormally() {
        addCreatureReady(player1, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        giant.tap();
        forest.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may choose no permanents to untap")
    void mayChooseNoPermanentsToUntap() {
        addCreatureReady(player1, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(giant.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(petal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's untapped Static Orb restricts your untap step too")
    void opponentStaticOrbRestrictsYourUntap() {
        addCreatureReady(player2, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId()));

        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(petal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A permanent with an optional untap ability can be one of the two chosen permanents")
    void mayChoosePermanentWithOptionalUntap() {
        addCreatureReady(player1, new StaticOrb());
        Permanent helm = addCreatureReady(player1, new HelmOfPossession());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        helm.tap();
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(helm.getId(), forest.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(helm.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(giant.isTapped()).isTrue();
        assertThat(petal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Static Orb and Winter Orb both restrict the same untap choice")
    void overlappingOrbsEnforceBothCaps() {
        addCreatureReady(player1, new StaticOrb());
        addCreatureReady(player1, new WinterOrb());
        Permanent firstForest = addCreatureReady(player1, new Forest());
        Permanent secondForest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        firstForest.tap();
        secondForest.tap();
        petal.tap();

        advanceToNextTurn(player2);

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(firstForest.getId(), secondForest.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstForest.getId(), petal.getId()));

        assertThat(firstForest.isTapped()).isFalse();
        assertThat(secondForest.isTapped()).isTrue();
        assertThat(petal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Two Static Orbs still allow two permanents to untap")
    void multipleStaticOrbsDoNotReduceCap() {
        addCreatureReady(player1, new StaticOrb());
        addCreatureReady(player2, new StaticOrb());
        Permanent giant = addCreatureReady(player1, new LowlandGiant());
        Permanent forest = addCreatureReady(player1, new Forest());
        Permanent petal = addCreatureReady(player1, new LotusPetal());
        giant.tap();
        forest.tap();
        petal.tap();

        advanceToNextTurn(player2);
        harness.handleMultiplePermanentsChosen(player1, List.of(giant.getId(), forest.getId()));

        assertThat(giant.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(petal.isTapped()).isTrue();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
    }
}
