package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FrilledOculus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarkForDeath.class, FrilledOculus.class})
class MarkForDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps the target, forces it to block, and shuts out the controller's other creatures")
    void resolvesAllThreeEffects() {
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        Permanent other = addCreatureReady(player2, new FrilledOculus());
        target.tap();
        castMarkForDeath(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.isMustBlockThisTurnIfAble()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isFalse();
        assertThat(other.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Leaves the caster's own creatures able to block")
    void doesNotAffectCastersCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        castMarkForDeath(target);

        assertThat(ownCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Declaring no blockers is illegal — the marked creature must block")
    void markedCreatureMustBlock() {
        Permanent attacker = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        addCreatureReady(player2, new FrilledOculus());
        castMarkForDeath(target);

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
    }

    @Test
    @DisplayName("The marked creature blocking satisfies the requirement, the other creature can't join")
    void onlyMarkedCreatureCanBlock() {
        Permanent attacker = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        addCreatureReady(player2, new FrilledOculus());
        castMarkForDeath(target);

        beginCombat(attacker);

        // Defender index 0 is the marked creature; index 1 is the shut-out one.
        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Blocking with a shut-out creature is illegal")
    void otherCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        addCreatureReady(player2, new FrilledOculus());
        castMarkForDeath(target);

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can't target a creature its caster controls")
    void cannotTargetOwnCreature() {
        Permanent ownCreature = addCreatureReady(player1, new FrilledOculus());
        addCreatureReady(player2, new FrilledOculus());
        harness.setHand(player1, List.of(new MarkForDeath()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Other creatures entering after resolution cannot block either")
    void creatureEnteringLaterCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        castMarkForDeath(target);
        harness.addToBattlefield(player2, new FrilledOculus());

        beginCombat(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A marked creature tapped again before combat is not required to block")
    void tappedMarkedCreatureCannotBeForcedToBlock() {
        Permanent attacker = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        castMarkForDeath(target);
        target.tap();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An illegal target prevents the restriction on other creatures from resolving")
    void removedTargetPreventsAllEffects() {
        Permanent attacker = addCreatureReady(player1, new FrilledOculus());
        Permanent target = addCreatureReady(player2, new FrilledOculus());
        addCreatureReady(player2, new FrilledOculus());
        harness.setHand(player1, List.of(new MarkForDeath()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        beginCombat(attacker);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    private void castMarkForDeath(Permanent target) {
        harness.setHand(player1, List.of(new MarkForDeath()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void beginCombat(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }
}
