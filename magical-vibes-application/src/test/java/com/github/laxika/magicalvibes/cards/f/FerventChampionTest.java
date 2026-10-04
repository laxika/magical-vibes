package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.c.CrystalSlipper;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LizardBlades;
import com.github.laxika.magicalvibes.cards.v.VenerableKnight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FerventChampion.class, VenerableKnight.class, GrizzlyBears.class, LeoninScimitar.class,
        LizardBlades.class, Frogify.class, Gingerbrute.class, CrystalSlipper.class})
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

    @Test
    void hasteAllowsAttackingAloneImmediatelyWithoutBoostingItself() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new FerventChampion());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(champion.isAttacking()).isTrue();
        assertThat(champion.getPowerModifier()).isZero();
    }

    @Test
    void firstStrikeKillsABlockerBeforeItCanDealDamage() {
        addCreatureReady(player1, new FerventChampion());
        harness.addToBattlefield(player2, new Gingerbrute());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Fervent Champion");
        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertLife(player2, 20);
    }

    @Test
    void attackTriggerResolvesAfterChampionLeavesTheBattlefield() {
        Permanent champion = addCreatureReady(player1, new FerventChampion());
        Permanent knight = addCreatureReady(player1, new VenerableKnight());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, knight.getId());
        gd.playerBattlefields.get(player1.getId()).remove(champion);
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isEqualTo(1);
        assertThat(knight.getToughnessModifier()).isZero();
    }

    @Test
    void attackTriggerDoesNotBoostAKnightRemovedFromCombat() {
        addCreatureReady(player1, new FerventChampion());
        Permanent knight = addCreatureReady(player1, new VenerableKnight());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, knight.getId());
        knight.setAttacking(false);
        resolveAllTriggers();

        assertThat(knight.getPowerModifier()).isZero();
    }

    @Test
    void reconfigureTargetingChampionStillRequiresItsFullManaCost() {
        Permanent champion = addCreatureReady(player1, new FerventChampion());
        addCreatureReady(player1, new LizardBlades());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, champion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void losingAllAbilitiesRemovesTheEquipDiscount() {
        Permanent champion = addCreatureReady(player1, new FerventChampion());
        Permanent frogify = harness.addToBattlefieldAndReturn(player1, new Frogify());
        frogify.setAttachedTo(champion.getId());
        harness.addToBattlefield(player1, new CrystalSlipper());

        assertThat(gqs.hasLostAllAbilities(gd, champion)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, champion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
