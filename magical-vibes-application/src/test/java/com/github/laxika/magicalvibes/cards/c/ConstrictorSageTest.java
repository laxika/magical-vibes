package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KrotiqNestguard;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({ConstrictorSage.class, KrotiqNestguard.class, Mountain.class})
class ConstrictorSageTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps and stuns target creature an opponent controls")
    void etbTapsAndStunsOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setHand(player1, List.of(new ConstrictorSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renew taps and stuns target creature and exiles the source")
    void renewTapsAndStunsOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Constrictor Sage");
    }

    @Test
    @DisplayName("Renew requires a creature an opponent controls")
    void renewRejectsIllegalTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setGraveyard(player1, List.of(new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Renew can only be activated as a sorcery")
    void renewIsSorcerySpeedOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Constrictor Sage enters even when no opponent controls a creature")
    void entersWithoutLegalEtbTarget() {
        harness.addToBattlefield(player1, new KrotiqNestguard());
        harness.addToBattlefield(player2, new Mountain());

        harness.enterBattlefieldAndReturn(player1, new ConstrictorSage());

        harness.assertOnBattlefield(player1, "Constrictor Sage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allSatisfy(permanent -> {
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.getCounterCount(CounterType.STUN)).isZero();
                });
    }

    @Test
    @DisplayName("ETB stuns an already tapped creature and prevents its next untap")
    void etbStunsAlreadyTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        target.tap();
        harness.setHand(player1, List.of(new ConstrictorSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Renew exiles its source immediately and stacks stun counters on a tapped creature")
    void renewPaysExileCostBeforeResolutionAndAddsAnotherStunCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage(), new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.passBothPriorities();

        harness.activateGraveyardAbility(player1, 0, target.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Renew rejects a creature its controller controls without exiling the source")
    void renewRejectsOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Constrictor Sage");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Renew cannot be activated during its controller's upkeep")
    void renewRejectsNonMainPhase() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Constrictor Sage");
    }

    @Test
    @DisplayName("Renew cannot be activated while another ability is on the stack")
    void renewRequiresEmptyStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage(), new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0, target.getId());
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Renew requires blue mana and does not exile its source when payment fails")
    void renewRequiresBlueMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KrotiqNestguard());
        harness.setGraveyard(player1, List.of(new ConstrictorSage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Constrictor Sage");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }
}
