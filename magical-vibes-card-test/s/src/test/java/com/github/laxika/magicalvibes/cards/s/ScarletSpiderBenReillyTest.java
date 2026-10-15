package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarletSpiderBenReilly.class, GrizzlyBears.class})
class ScarletSpiderBenReillyTest extends BaseCardTest {
    @Test
    @DisplayName("Web-slinging can return another Scarlet Spider and uses its full mana value")
    void canReturnAnotherScarletSpider() {
        Permanent returned = harness.addToBattlefieldAndReturn(player1, new ScarletSpiderBenReilly());
        returned.tap();
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(returned.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(returned.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(3));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging cannot return an untapped creature")
    void cannotReturnUntappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ScarletSpiderBenReilly());
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging cannot return an opponent's tapped creature")
    void cannotReturnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScarletSpiderBenReilly());
        creature.tap();
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast gives no Sensational Save counters")
    void enteringWithoutCastingDoesNotGetCounters() {
        Permanent spider = harness.enterBattlefieldAndReturn(player1, new ScarletSpiderBenReilly());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Web-slinging gives Scarlet Spider counters equal to the returned creature's mana value")
    void webSlingingUsesReturnedCreatureManaValue() {
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(tappedCreature.getId()));
        harness.passBothPriorities();

        Permanent scarletSpider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ScarletSpiderBenReilly)
                .findFirst()
                .orElseThrow();
        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).contains(tappedCreature.getCard());
    }

    @Test
    @DisplayName("A normal cast does not get Sensational Save counters")
    void normalCastDoesNotGetCounters() {
        harness.setHand(player1, List.of(new ScarletSpiderBenReilly()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scarletSpider = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof ScarletSpiderBenReilly)
                .findFirst()
                .orElseThrow();
        assertThat(scarletSpider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
