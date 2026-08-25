package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerventChampion.class, VenerableKnight.class, GrizzlyBears.class, LeoninScimitar.class})
class FerventChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking targets another attacking Knight you control")
    void attackingTargetsAnotherAttackingKnightYouControl() {
        Permanent champion = addCreatureReady(player1, new FerventChampion());
        Permanent attackingKnight = addCreatureReady(player1, new VenerableKnight());
        Permanent nonAttackingKnight = addCreatureReady(player1, new VenerableKnight());
        Permanent attackingNonKnight = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttackingKnight = addCreatureReady(player2, new VenerableKnight());
        opponentAttackingKnight.setAttacking(true);

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attackingKnight.getId());

        harness.handlePermanentChosen(player1, attackingKnight.getId());
        resolveAllTriggers();

        assertThat(attackingKnight.getPowerModifier()).isEqualTo(1);
        assertThat(champion.getPowerModifier()).isZero();
        assertThat(nonAttackingKnight.getPowerModifier()).isZero();
        assertThat(attackingNonKnight.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FerventChampion());
        Permanent attackingKnight = addCreatureReady(player1, new VenerableKnight());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attackingKnight.getId());
        resolveAllTriggers();

        assertThat(attackingKnight.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attackingKnight.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Equip abilities targeting Fervent Champion cost three less")
    void equipmentTargetingChampionIsReduced() {
        Permanent champion = addCreatureReady(player1, new FerventChampion());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                champion.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(champion.getId());
    }

    @Test
    @DisplayName("Equip abilities targeting another creature are not reduced")
    void equipmentTargetingAnotherCreatureIsNotReduced() {
        addCreatureReady(player1, new FerventChampion());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent otherCreature = addCreatureReady(player1, new VenerableKnight());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                otherCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
