package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BloodscaleProwler;
import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.cards.g.GiantSolifuge;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UlashtTheHateSeed.class, BloodscaleProwler.class, GhorClanSavage.class,
        GiantSolifuge.class, IzzetSignet.class})
class UlashtTheHateSeedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a counter for each other red creature and each other green creature you control")
    void entersWithCountersForControlledColors() {
        harness.addToBattlefield(player1, new GhorClanSavage());
        harness.addToBattlefield(player1, new BloodscaleProwler());
        harness.addToBattlefield(player1, new GiantSolifuge());
        harness.addToBattlefield(player2, new GhorClanSavage());
        harness.addToBattlefield(player2, new BloodscaleProwler());

        harness.setHand(player1, List.of(new UlashtTheHateSeed()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ulasht = findPermanent(player1, "Ulasht, the Hate Seed");
        assertThat(ulasht.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing a counter lets Ulasht deal 1 damage to a target creature")
    void damageAbilityRemovesCounterAndDealsDamage() {
        Permanent ulasht = addReadyUlasht();
        ulasht.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhorClanSavage());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(ulasht.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage ability cannot target a noncreature permanent")
    void damageAbilityRequiresCreatureTarget() {
        Permanent ulasht = addReadyUlasht();
        ulasht.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, signet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("An activated ability cannot be paid without a +1/+1 counter")
    void activatedAbilityRequiresCounter() {
        addReadyUlasht();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counters");
    }

    @Test
    @DisplayName("Removing a counter creates a 1/1 green Saproling token")
    void tokenAbilityRemovesCounterAndCreatesSaproling() {
        Permanent ulasht = addReadyUlasht();
        ulasht.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ulasht.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SAPROLING)
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
    }

    private Permanent addReadyUlasht() {
        return addReadyUlasht(player1);
    }

    private Permanent addReadyUlasht(Player player) {
        return addCreatureReady(player, new UlashtTheHateSeed());
    }
}
