package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BastionProtector.class, GrizzlyBears.class})
class BastionProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts commander creatures you control and grants indestructible")
    void boostsOwnCommanderCreatures() {
        harness.addToBattlefield(player1, new BastionProtector());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nonCommander = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCommander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(2);

        gd.makeCommander(player1.getId(), commander.getCard());
        gd.makeCommander(player2.getId(), opponentCommander.getCard());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isTrue();

        assertThat(gqs.getEffectivePower(gd, nonCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonCommander, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCommander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Boosts Bastion Protector if it is a commander")
    void boostsItselfWhenItIsACommander() {
        Permanent bastion = harness.addToBattlefieldAndReturn(player1, new BastionProtector());

        gd.makeCommander(player1.getId(), bastion.getCard());

        assertThat(gqs.getEffectivePower(gd, bastion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bastion)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bastion, Keyword.INDESTRUCTIBLE)).isTrue();
    }
}
