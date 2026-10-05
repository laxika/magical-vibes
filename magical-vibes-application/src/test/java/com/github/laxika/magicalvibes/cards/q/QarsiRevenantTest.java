package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SaltRoadPackbeast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QarsiRevenant.class, SaltRoadPackbeast.class, Mountain.class})
class QarsiRevenantTest extends BaseCardTest {

    private void readyRenew() {
        harness.setGraveyard(player1, List.of(new QarsiRevenant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Renew puts flying, deathtouch, and lifelink counters on target creature")
    void renewPutsKeywordCountersOnTargetCreature() {
        Permanent creature = addCreatureReady(player2, new SaltRoadPackbeast());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Renew exiles Qarsi Revenant as an activation cost")
    void renewExilesSource() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, creature.getId());

        harness.assertNotInGraveyard(player1, "Qarsi Revenant");
    }

    @Test
    @DisplayName("Renew requires a creature target")
    void renewRequiresCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Renew can only be activated at sorcery speed")
    void renewIsSorcerySpeedOnly() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        harness.setGraveyard(player1, List.of(new QarsiRevenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void renewWorksInPostcombatMainPhase() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        readyRenew();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isEqualTo(1);
        assertThat(creature.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName()).containsExactly("Qarsi Revenant");
    }

    @Test
    void renewCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        readyRenew();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Qarsi Revenant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void renewCannotBeActivatedWithAnAbilityOnTheStack() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        readyRenew();
        harness.setGraveyard(player1, List.of(new QarsiRevenant(), new QarsiRevenant()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Qarsi Revenant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void renewRequiresBlackManaAndDoesNotExileSourceOnFailure() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        harness.setGraveyard(player1, List.of(new QarsiRevenant()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Qarsi Revenant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void renewRequiresATargetAndDoesNotPayCostsWithoutOne() {
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Qarsi Revenant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void renewDoesNotPlaceCountersWhenTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new SaltRoadPackbeast());
        readyRenew();
        harness.activateGraveyardAbility(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(creature.getCounterCount(CounterType.DEATHTOUCH)).isZero();
        assertThat(creature.getCounterCount(CounterType.LIFELINK)).isZero();
        harness.assertNotInGraveyard(player1, "Qarsi Revenant");
        assertThat(gd.exiledCards)
                .extracting(entry -> entry.card().getName()).containsExactly("Qarsi Revenant");
        assertThat(gd.stack).isEmpty();
    }
}
