package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HardyVeteran;
import com.github.laxika.magicalvibes.cards.m.MistCloakedHerald;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeeprootElite.class, MistCloakedHerald.class, HardyVeteran.class, MerrowCommerce.class})
class DeeprootEliteTest extends BaseCardTest {

    @Test
    void doesNotTriggerForItsOwnEntry() {
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new MistCloakedHerald());
        harness.castFromHand(player1, new DeeprootElite(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsMerfolkEnteringDoesNotTrigger() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new DeeprootElite());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canPutCounterOnItself() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new DeeprootElite());
        harness.castFromHand(player1, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elite.getId());
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canPutCounterOnEnteringMerfolk() {
        harness.addToBattlefield(player1, new DeeprootElite());
        harness.castFromHand(player1, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();
        Permanent entrant = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof MistCloakedHerald)
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, entrant.getId());
        harness.passBothPriorities();

        assertThat(entrant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetOpponentsMerfolk() {
        harness.addToBattlefield(player1, new DeeprootElite());
        Permanent opponentMerfolk = harness.addToBattlefieldAndReturn(player2, new MistCloakedHerald());
        harness.castFromHand(player1, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentMerfolk.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void noncreatureMerfolkEnteringTriggersAbility() {
        Permanent elite = harness.addToBattlefieldAndReturn(player1, new DeeprootElite());
        harness.castFromHand(player1, new MerrowCommerce(), "{1}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, elite.getId());
        harness.passBothPriorities();

        assertThat(elite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canPutCounterOnNoncreatureMerfolk() {
        harness.addToBattlefield(player1, new DeeprootElite());
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
        harness.castFromHand(player1, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, commerce.getId());
        harness.passBothPriorities();

        assertThat(commerce.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Merfolk entering puts a +1/+1 counter on target Merfolk you control")
    void merfolkEnteringPutsCounterOnTargetMerfolk() {
        harness.addToBattlefield(player1, new DeeprootElite());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new MistCloakedHerald());

        harness.castFromHand(player1, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Merfolk entering does not trigger the ability")
    void nonMerfolkEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new DeeprootElite());
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new MistCloakedHerald());

        harness.castFromHand(player1, new HardyVeteran(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature you control")
    void cannotTargetNonMerfolk() {
        harness.addToBattlefield(player1, new DeeprootElite());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new HardyVeteran());

        harness.castFromHand(player1, new MistCloakedHerald(), "{U}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }
}
