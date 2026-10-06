package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HungryGhoul.class, BearCub.class})
class HungryGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, sacrifice another creature: the Ghoul gets a +1/+1 counter")
    void sacrificeAnotherCreatureAddsCounter() {
        Permanent ghoul = addCreatureReady(player1, new HungryGhoul());
        addCreatureReady(player1, new BearCub());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bear Cub");
        harness.assertInGraveyard(player1, "Bear Cub");
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ghoul.getEffectivePower()).isEqualTo(3);
        assertThat(ghoul.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot sacrifice the Ghoul itself to its own ability")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new HungryGhoul());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Repeated activations accumulate counters")
    void repeatedActivationsAccumulateCounters() {
        Permanent ghoul = addCreatureReady(player1, new HungryGhoul());
        addCreatureReady(player1, new BearCub());
        addCreatureReady(player1, new BearCub());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Bear Cub").getId());
        harness.passBothPriorities();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifice is paid before the counter ability resolves")
    void sacrificeIsPaidAsAnActivationCost() {
        Permanent ghoul = addCreatureReady(player1, new HungryGhoul());
        addCreatureReady(player1, new BearCub());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Bear Cub");
        harness.assertInGraveyard(player1, "Bear Cub");
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ghoul can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ghoul = harness.addToBattlefieldAndReturn(player1, new HungryGhoul());
        ghoul.setSummoningSick(true);
        ghoul.tap();
        harness.addToBattlefield(player1, new BearCub());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bear Cub");
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ghoul.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent ghoul = addCreatureReady(player1, new HungryGhoul());
        Permanent opponentCreature = addCreatureReady(player2, new BearCub());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A pending ability cannot put its counter on another Ghoul after its source leaves")
    void sourceLeavingDoesNotRedirectCounter() {
        Permanent originalGhoul = addCreatureReady(player1, new HungryGhoul());
        Permanent survivingGhoul = addCreatureReady(player1, new HungryGhoul());
        Permanent cub = addCreatureReady(player1, new BearCub());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, cub.getId());
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(originalGhoul);
        harness.assertInGraveyard(player1, "Hungry Ghoul");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivingGhoul);
        assertThat(survivingGhoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability requires one mana as well as another creature")
    void cannotActivateWithoutMana() {
        Permanent ghoul = addCreatureReady(player1, new HungryGhoul());
        Permanent sacrificeCandidate = addCreatureReady(player1, new BearCub());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ghoul, sacrificeCandidate);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(ghoul.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
