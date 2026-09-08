package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadhordeInvasion.class})
class DreadhordeInvasionTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep loses 1 life and amasses a Zombie Army when no Army is controlled")
    void upkeepCreatesZombieArmyWhenNoArmyIsControlled() {
        harness.addToBattlefield(player1, new DreadhordeInvasion());
        harness.setLife(player1, 20);

        advanceAndResolveUpkeep();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        Permanent army = findPermanent(player1, "Zombie Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Amass puts the counter on the existing Army instead of creating another token")
    void upkeepAmassesOnExistingArmy() {
        harness.addToBattlefield(player1, new DreadhordeInvasion());
        advanceAndResolveUpkeep();
        Permanent army = findPermanent(player1, "Zombie Army");

        advanceAndResolveUpkeep();

        assertThat(findPermanents(player1, "Zombie Army")).hasSize(1);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A Zombie token with power 6 gains lifelink when it attacks")
    void powerfulZombieTokenGainsLifelinkWhenItAttacks() {
        harness.addToBattlefield(player1, new DreadhordeInvasion());
        advanceAndResolveUpkeep();
        Permanent army = findPermanent(player1, "Zombie Army");
        army.setSummoningSick(false);
        army.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);

        declareAttackers(List.of(1));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(army.getGrantedKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("The lifelink trigger uses the token's power when it attacks")
    void powerIsCheckedWhenTokenAttacks() {
        harness.addToBattlefield(player1, new DreadhordeInvasion());
        advanceAndResolveUpkeep();
        Permanent army = findPermanent(player1, "Zombie Army");
        army.setSummoningSick(false);
        army.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 6);

        declareAttackers(List.of(1));
        army.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(army.getGrantedKeywords()).contains(Keyword.LIFELINK);
    }

    @Test
    @DisplayName("A Zombie token with power less than 6 does not gain lifelink")
    void weakZombieTokenDoesNotGainLifelink() {
        harness.addToBattlefield(player1, new DreadhordeInvasion());
        advanceAndResolveUpkeep();
        Permanent army = findPermanent(player1, "Zombie Army");
        army.setSummoningSick(false);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(army.getGrantedKeywords()).doesNotContain(Keyword.LIFELINK);
    }

    private void advanceAndResolveUpkeep() {
        advanceToUpkeep(player1);
        resolveAllTriggers();
    }
}
