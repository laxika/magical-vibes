package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KrumarInitiate;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({ChampionOfDusan.class, KrumarInitiate.class, Mountain.class})
class ChampionOfDusanTest extends BaseCardTest {

    private void readyRenew() {
        harness.setGraveyard(player1, List.of(new ChampionOfDusan()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Renew puts a +1/+1 counter and a trample counter on target creature")
    void renewPutsCountersOnTargetCreature() {
        Permanent bears = addCreatureReady(player1, new KrumarInitiate());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Renew exiles Champion of Dusan as an activation cost")
    void renewExilesSource() {
        Permanent bears = addCreatureReady(player1, new KrumarInitiate());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, bears.getId());

        harness.assertNotInGraveyard(player1, "Champion of Dusan");
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
    @DisplayName("Renew can only be activated as a sorcery")
    void renewIsSorcerySpeedOnly() {
        Permanent bears = addCreatureReady(player1, new KrumarInitiate());
        harness.setGraveyard(player1, List.of(new ChampionOfDusan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void renewCanTargetAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new KrumarInitiate());
        readyRenew();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void renewPaysManaAndExilesTheSourceBeforeCountersArePlaced() {
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        readyRenew();
        var source = gd.playerGraveyards.get(player1.getId()).getFirst();

        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card()).isSameAs(source);
            assertThat(entry.ownerId()).isEqualTo(player1.getId());
        });
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isZero();

        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
    }

    @Test
    void renewCanBeActivatedInPostcombatMainPhase() {
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        readyRenew();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
    }

    @Test
    void renewCannotBeActivatedOutsideAMainPhase() {
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        readyRenew();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Champion of Dusan");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void renewCannotBeActivatedWithAnAbilityOnTheStack() {
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        readyRenew();
        harness.setGraveyard(player1, List.of(new ChampionOfDusan(), new ChampionOfDusan()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isEqualTo(1);
    }

    @Test
    void renewRequiresGreenManaAndDoesNotExileWhenPaymentFails() {
        Permanent target = addCreatureReady(player1, new KrumarInitiate());
        harness.setGraveyard(player1, List.of(new ChampionOfDusan()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Champion of Dusan");
        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.TRAMPLE)).isZero();
    }

    @Test
    void renewRequiresATargetBeforePayingCosts() {
        readyRenew();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Champion of Dusan");
        assertThat(gd.stack).isEmpty();
    }
}
