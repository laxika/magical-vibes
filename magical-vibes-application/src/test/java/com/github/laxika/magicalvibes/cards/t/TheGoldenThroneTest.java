package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGoldenThrone.class, GrizzlyBears.class})
class TheGoldenThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and sets its controller's life total to 1 instead of losing")
    void replacesGameLoss() {
        var throne = harness.addToBattlefieldAndReturn(player1, new TheGoldenThrone());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(throne);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(throne.getCard());
    }

    @Test
    @DisplayName("Tapping and sacrificing a creature adds three mana in the chosen combination")
    void sacrificeCreatureAddsThreeManaInAnyCombination() {
        harness.addToBattlefield(player1, new TheGoldenThrone());
        Permanent sacrificedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedBear.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "BLUE");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificedBear);
    }
}
