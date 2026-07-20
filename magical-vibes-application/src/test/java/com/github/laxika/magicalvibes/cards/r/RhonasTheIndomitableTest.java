package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RhonasTheIndomitableTest extends BaseCardTest {

    // ===== Attack restriction: another creature with power 4 or greater =====

    @Test
    @DisplayName("Can attack when controlling another creature with power 4 or greater")
    void canAttackWithPowerFourCreature() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("Cannot attack when only controlling a creature with power 3")
    void cannotAttackWithoutPowerFourCreature() {
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player1, new HillGiant());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Block restriction =====

    @Test
    @DisplayName("Can block when controlling another creature with power 4 or greater")
    void canBlockWithPowerFourCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player1, new AirElemental());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(findPermanent(player1, "Rhonas the Indomitable").isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot block when only controlling a creature with power 3")
    void cannotBlockWithoutPowerFourCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Activated ability: another target creature gets +2/+0 and gains trample =====

    @Test
    @DisplayName("Activated ability gives target creature +2/+0 and trample")
    void abilityBoostsAndGrantsTrample() {
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Ability can target an opponent's creature")
    void abilityCanTargetOpponentCreature() {
        addCreatureReady(player1, new RhonasTheIndomitable());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Ability cannot target itself")
    void abilityCannotTargetSelf() {
        addCreatureReady(player1, new RhonasTheIndomitable());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID rhonasId = harness.getPermanentId(player1, "Rhonas the Indomitable");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rhonasId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helper methods =====

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
