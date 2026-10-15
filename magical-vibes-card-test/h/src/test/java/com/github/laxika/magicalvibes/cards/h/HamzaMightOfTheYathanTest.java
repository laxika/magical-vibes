package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfDeceit;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HamzaMightOfTheYathan.class, Forest.class, GrizzlyBears.class, WallOfDeceit.class})
class HamzaMightOfTheYathanTest extends BaseCardTest {

    @Test
    void landfallSeeksCreatureAndManifestsIt() {
        Card soughtCreature = new GrizzlyBears();
        Card remainingLand = new Forest();
        harness.addToBattlefield(player1, new HamzaMightOfTheYathan());
        harness.setLibrary(player1, List.of(soughtCreature, remainingLand));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(soughtCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(soughtCreature.getId()));
    }

    @Test
    void turningCreatureFaceUpMakesItEndureByItsToughness() {
        harness.addToBattlefield(player1, new HamzaMightOfTheYathan());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfDeceit());
        wall.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(wall));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 5 +1/+1 counters on this permanent");

        assertThat(wall.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }
}
