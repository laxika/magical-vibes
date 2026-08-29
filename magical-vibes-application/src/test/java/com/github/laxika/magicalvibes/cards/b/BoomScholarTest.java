package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GreasewrenchGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonRoost;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Boom Scholar")
class BoomScholarTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces another permanent's exhaust ability by two generic mana")
    void reducesAnotherExhaustAbility() {
        harness.addToBattlefield(player1, new BoomScholar());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not reduce its own exhaust ability or an ordinary ability")
    void onlyReducesOtherExhaustAbilities() {
        harness.addToBattlefield(player1, new BoomScholar());
        harness.addToBattlefield(player1, new DragonRoost());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Grants trample to controlled creatures and Vehicles and adds two counters")
    void exhaustGrantsTrampleAndCounters() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new BoomScholar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new Boommobile());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scholar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, scholar, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The exhaust ability can be activated only once")
    void exhaustCanBeActivatedOnlyOnce() {
        harness.addToBattlefield(player1, new BoomScholar());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }
}
