package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.l.LoamDryad;
import com.github.laxika.magicalvibes.cards.q.QuilledWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DissensionInTheRanks.class, QuilledWolf.class, LoamDryad.class, JustTheWind.class})
class DissensionInTheRanksTest extends BaseCardTest {

    @Test
    @DisplayName("Two blocking creatures fight")
    void twoBlockingCreaturesFight() {
        prepareSpell();
        Permanent firstBlocker = addCreatureReady(player2, new QuilledWolf());
        Permanent secondBlocker = addCreatureReady(player2, new LoamDryad());
        declareSeparateBlocks(firstBlocker, secondBlocker);

        castDissensionInTheRanks(firstBlocker, secondBlocker);

        harness.assertOnBattlefield(player2, "Quilled Wolf");
        harness.assertInGraveyard(player2, "Loam Dryad");
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("A creature that is not blocking cannot be either target")
    void nonBlockingCreatureCannotBeTargeted(boolean invalidFirstTarget) {
        prepareSpell();
        Permanent blocker = addCreatureReady(player2, new QuilledWolf());
        Permanent secondBlocker = addCreatureReady(player2, new QuilledWolf());
        Permanent bystander = addCreatureReady(player2, new LoamDryad());
        declareSeparateBlocks(blocker, secondBlocker);

        List<java.util.UUID> targets = invalidFirstTarget
                ? List.of(bystander.getId(), blocker.getId())
                : List.of(blocker.getId(), bystander.getId());
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The same blocker cannot be both targets")
    void targetsMustBeDifferent() {
        prepareSpell();
        Permanent firstBlocker = addCreatureReady(player2, new QuilledWolf());
        Permanent secondBlocker = addCreatureReady(player2, new QuilledWolf());
        declareSeparateBlocks(firstBlocker, secondBlocker);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(firstBlocker.getId(), firstBlocker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Both blockers deal lethal fight damage to each other")
    void bothBlockersDie() {
        prepareSpell();
        Permanent firstBlocker = addCreatureReady(player2, new QuilledWolf());
        Permanent secondBlocker = addCreatureReady(player2, new QuilledWolf());
        declareSeparateBlocks(firstBlocker, secondBlocker);

        castDissensionInTheRanks(firstBlocker, secondBlocker);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Dissension in the Ranks");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Neither blocker fights if either target leaves before resolution")
    void noFightWhenEitherTargetLeaves(boolean removeFirstTarget) {
        prepareSpell();
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        Permanent firstBlocker = addCreatureReady(player2, new QuilledWolf());
        Permanent secondBlocker = addCreatureReady(player2, new QuilledWolf());
        declareSeparateBlocks(firstBlocker, secondBlocker);
        Permanent removed = removeFirstTarget ? firstBlocker : secondBlocker;
        Permanent remaining = removeFirstTarget ? secondBlocker : firstBlocker;

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castInstant(player1, 0, List.of(firstBlocker.getId(), secondBlocker.getId()));
            gs.passPriority(gd, player1);
            harness.castAndResolveInstant(player2, 0, removed.getId());
            harness.passBothPriorities();
        });

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(remaining);
        assertThat(remaining.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Quilled Wolf");
        harness.assertInGraveyard(player1, "Dissension in the Ranks");
        assertThat(gd.stack).isEmpty();
    }

    private void declareSeparateBlocks(Permanent firstBlocker, Permanent secondBlocker) {
        Permanent firstAttacker = addCreatureReady(player1, new QuilledWolf());
        Permanent secondAttacker = addCreatureReady(player1, new QuilledWolf());
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(firstBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(firstAttacker)),
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(secondBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(secondAttacker)))));
    }

    private void castDissensionInTheRanks(Permanent firstTarget, Permanent secondTarget) {
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () ->
                harness.castAndResolveInstant(player1, 0, List.of(firstTarget.getId(), secondTarget.getId())));
    }

    private void prepareSpell() {
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setHand(player1, List.of(new DissensionInTheRanks()));
    }
}
