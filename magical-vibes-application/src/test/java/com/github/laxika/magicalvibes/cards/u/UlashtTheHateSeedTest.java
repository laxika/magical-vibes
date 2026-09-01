package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GhorClanRampager;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({UlashtTheHateSeed.class, GhorClanRampager.class, GrizzlyBears.class,
        RagingGoblin.class, Island.class})
class UlashtTheHateSeedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a counter for each other red creature and each other green creature you control")
    void entersWithCountersForControlledColors() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new GhorClanRampager());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GhorClanRampager());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
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
