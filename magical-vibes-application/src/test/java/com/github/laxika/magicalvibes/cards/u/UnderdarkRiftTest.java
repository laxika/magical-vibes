package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderdarkRift.class, GrizzlyBears.class})
class UnderdarkRiftTest extends BaseCardTest {

    @Test
    @DisplayName("Rolls a d10 and puts the target just beneath that many cards")
    void rollsAndPutsTargetIntoLibrary() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, new ArrayList<>());
        for (int i = 0; i < 11; i++) {
            gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rift.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player2.getId())).contains(target.getCard());
        assertThat(gd.playerDecks.get(player2.getId()).indexOf(target.getCard())).isBetween(1, 10);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent rift = harness.addToBattlefieldAndReturn(player1, new UnderdarkRift());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rift.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rift);
        assertThat(rift.isTapped()).isFalse();
    }
}
