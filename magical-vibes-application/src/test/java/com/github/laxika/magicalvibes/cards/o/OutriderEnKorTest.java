package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FledglingMawcor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OutriderEnKor.class, FledglingMawcor.class})
class OutriderEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects damage to a creature you control")
    void redirectsDamageToControlledCreature() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        Permanent mawcor = addCreatureReady(player1, new FledglingMawcor());
        Permanent destination = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, indexOf(player1, outrider), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, mawcor), null, outrider.getId());
        harness.passBothPriorities();

        assertThat(outrider.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the next 1 damage is redirected")
    void redirectsOnlyOneDamage() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        Permanent firstMawcor = addCreatureReady(player1, new FledglingMawcor());
        Permanent secondMawcor = addCreatureReady(player1, new FledglingMawcor());
        Permanent destination = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, indexOf(player1, outrider), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, firstMawcor), null, outrider.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondMawcor), null, outrider.getId());
        harness.passBothPriorities();

        assertThat(destination.getMarkedDamage()).isEqualTo(1);
        assertThat(outrider.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The redirect lasts only until the end of the turn")
    void redirectExpiresAtEndOfTurn() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        Permanent destination = addCreatureReady(player1, new FledglingMawcor());
        Permanent mawcor = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, indexOf(player1, outrider), null, destination.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, mawcor), null, outrider.getId());
        harness.passBothPriorities();

        assertThat(outrider.getMarkedDamage()).isEqualTo(1);
        assertThat(destination.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The ability cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        Permanent opponentCreature = addCreatureReady(player2, new FledglingMawcor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, outrider), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a player")
    void cannotTargetPlayer() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, outrider), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingWeakensNonFlankingBlocker() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        outrider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FledglingMawcor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker that has flanking")
    void flankingDoesNotAffectFlankingBlocker() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        outrider.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new OutriderEnKor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirected damage can be redirected again by another en-Kor")
    void redirectsDamageThroughAnotherEnKor() {
        Permanent first = addCreatureReady(player1, new OutriderEnKor());
        Permanent second = addCreatureReady(player1, new OutriderEnKor());
        Permanent destination = addCreatureReady(player1, new FledglingMawcor());
        Permanent source = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, indexOf(player1, second), null, destination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, first), null, second.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, source), null, first.getId());
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isZero();
        assertThat(second.getMarkedDamage()).isZero();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability may target Outrider itself without preventing damage")
    void mayRedirectDamageToItself() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        Permanent source = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, indexOf(player1, outrider), null, outrider.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, source), null, outrider.getId());
        harness.passBothPriorities();

        assertThat(outrider.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple activations redirect separate points of damage")
    void multipleActivationsProtectAgainstSeparateDamageEvents() {
        Permanent outrider = addCreatureReady(player1, new OutriderEnKor());
        Permanent destination = addCreatureReady(player1, new OutriderEnKor());
        Permanent firstSource = addCreatureReady(player1, new FledglingMawcor());
        Permanent secondSource = addCreatureReady(player1, new FledglingMawcor());

        harness.activateAbility(player1, indexOf(player1, outrider), null, destination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, outrider), null, destination.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, firstSource), null, outrider.getId());
        harness.passBothPriorities();
        assertThat(destination.getMarkedDamage()).isEqualTo(1);

        harness.activateAbility(player1, indexOf(player1, secondSource), null, outrider.getId());
        harness.passBothPriorities();

        assertThat(outrider.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(outrider).doesNotContain(destination);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(destination.getCard());
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
