package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.p.Ponder;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({NardoleResourcefulCyborg.class, Ponder.class, Shock.class})
class NardoleResourcefulCyborgTest extends BaseCardTest {

    @Test
    @DisplayName("Nardole adds blue mana for every counter on it")
    void addsManaForEveryCounter() {
        Permanent nardole = addReadyNardole();
        nardole.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        nardole.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getNoncreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nardole's mana can cast noncreature spells but not creature spells")
    void manaIsRestrictedToNoncreatureSpells() {
        Permanent nardole = addReadyNardole();
        nardole.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player1, List.of(new NardoleResourcefulCyborg()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Undying returns Nardole with a +1/+1 counter")
    void undyingReturnsWithCounter() {
        Permanent nardole = addReadyNardole();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, nardole.getId());
        resolveAllTriggers();

        Permanent returnedNardole = findPermanent(player1, "Nardole, Resourceful Cyborg");
        assertThat(returnedNardole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Nardole, Resourceful Cyborg");
    }

    @Test
    void restrictedManaPaysForBlueNoncreatureSpell() {
        Permanent nardole = addReadyNardole();
        nardole.setCounterCount(CounterType.CHARGE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new Ponder()));

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getNoncreatureSpellOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void noCountersProducesNoManaButStillTaps() {
        Permanent nardole = addReadyNardole();

        harness.activateAbility(player1, 0, null, null);

        assertThat(nardole.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId())
                .getNoncreatureSpellOnlyMana(ManaColor.BLUE)).isZero();
    }

    @Test
    void summoningSicknessPreventsManaAbility() {
        Permanent nardole = addReadyNardole();
        nardole.setSummoningSick(true);
        nardole.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nardole.isTapped()).isFalse();
    }

    @Test
    void undyingDoesNotReturnWithExistingPlusOneCounter() {
        Permanent nardole = addReadyNardole();
        nardole.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, nardole.getId());
        resolveAllTriggers();
        harness.castInstant(player2, 0, nardole.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Nardole, Resourceful Cyborg");
        harness.assertNotOnBattlefield(player1, "Nardole, Resourceful Cyborg");
    }

    @Test
    void undyingIgnoresChargeCountersAndReturnsAsNewPermanent() {
        Permanent nardole = addReadyNardole();
        nardole.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, nardole.getId());
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Nardole, Resourceful Cyborg");
        assertThat(returned.getId()).isNotEqualTo(nardole.getId());
        assertThat(returned.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        returned.setSummoningSick(false);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getNoncreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void tappedNardoleCannotActivateAgain() {
        Permanent nardole = addReadyNardole();
        nardole.setCounterCount(CounterType.CHARGE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getNoncreatureSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
    }
    private Permanent addReadyNardole() {
        Permanent nardole = harness.addToBattlefieldAndReturn(player1, new NardoleResourcefulCyborg());
        nardole.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return nardole;
    }
}
