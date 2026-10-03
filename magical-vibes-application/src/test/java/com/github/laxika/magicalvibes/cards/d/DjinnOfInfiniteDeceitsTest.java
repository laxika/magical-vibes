package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjinnOfInfiniteDeceits.class, GrizzlyBears.class, HillGiant.class, KrenkoMobBoss.class})
class DjinnOfInfiniteDeceitsTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of two target nonlegendary creatures")
    void exchangesControlOfNonlegendaryCreatures() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opponent.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Rejects a legendary creature as a target")
    void rejectsLegendaryCreatureTarget() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new KrenkoMobBoss());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(own.getId(), legendary.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the exchange ability during combat")
    void cannotActivateDuringCombat() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(own.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("You can't activate this ability during combat.");
    }

    @Test
    @DisplayName("Two creatures with the same controller can be targeted but do not exchange control")
    void sameControllerTargetsDoNothing() {
        addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DjinnOfInfiniteDeceits());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DjinnOfInfiniteDeceits());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("The Djinn can exchange itself and the targets can be supplied in reverse controller order")
    void exchangesItselfWithOpponentTargetFirst() {
        Permanent source = addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DjinnOfInfiniteDeceits());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(opponent.getId(), source.getId()));
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(source).doesNotContain(opponent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent).doesNotContain(source);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The exchange does nothing when one target leaves before resolution")
    void missingTargetPreventsEntireExchange() {
        Permanent source = addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DjinnOfInfiniteDeceits());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DjinnOfInfiniteDeceits());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        gd.playerGraveyards.get(player2.getId()).add(opponent.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source, own);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(own);
    }

    @Test
    @DisplayName("The ability resolves after its source leaves the battlefield")
    void sourceLeavingDoesNotPreventExchange() {
        Permanent source = addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DjinnOfInfiniteDeceits());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DjinnOfInfiniteDeceits());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(own.getId(), opponent.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponent).doesNotContain(own);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(own).doesNotContain(opponent);
    }

    @Test
    @DisplayName("The two targets must be distinct creatures")
    void rejectsDuplicateTargets() {
        Permanent source = addCreatureReady(player1, new DjinnOfInfiniteDeceits());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(source.getId(), source.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
