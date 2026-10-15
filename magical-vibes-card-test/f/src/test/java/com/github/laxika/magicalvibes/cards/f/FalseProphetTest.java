package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BraidwoodCup;
import com.github.laxika.magicalvibes.cards.p.PlatedSpider;
import com.github.laxika.magicalvibes.cards.r.RecklessAbandon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FalseProphet.class, PlatedSpider.class, BraidwoodCup.class, RecklessAbandon.class})
class FalseProphetTest extends BaseCardTest {

    @Test
    @DisplayName("When False Prophet dies, it exiles all creatures on both battlefields")
    void diesExilesAllCreatures() {
        harness.addToBattlefield(player1, new FalseProphet());
        harness.addToBattlefield(player1, new PlatedSpider());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());
        harness.addToBattlefield(player2, new PlatedSpider());
        harness.addToBattlefield(player2, new BraidwoodCup());

        killFalseProphet(sacrificed);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Braidwood Cup");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Plated Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Plated Spider");
        harness.assertInGraveyard(player1, "False Prophet");
        harness.assertInGraveyard(player2, "Plated Spider");
    }

    @Test
    @DisplayName("Sacrificing False Prophet triggers exile before Reckless Abandon resolves")
    void sacrificeTriggersBeforeSpellResolves() {
        Permanent prophet = harness.addToBattlefieldAndReturn(player1, new FalseProphet());
        harness.addToBattlefield(player2, new PlatedSpider());
        harness.setHand(player1, List.of(new RecklessAbandon()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorceryWithSacrifice(player1, 0, player2.getId(), prophet.getId());

        harness.assertInGraveyard(player1, "False Prophet");
        harness.assertOnBattlefield(player2, "Plated Spider");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Plated Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Plated Spider");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiling a second False Prophet does not trigger its death ability")
    void exilingAnotherProphetDoesNotTriggerDeath() {
        harness.addToBattlefield(player1, new FalseProphet());
        harness.addToBattlefield(player2, new FalseProphet());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player2, new PlatedSpider());

        killFalseProphet(sacrificed);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "False Prophet");
        harness.assertNotInGraveyard(player2, "False Prophet");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("False Prophet");
        assertThat(gd.stack).isEmpty();
    }

    private void killFalseProphet(Permanent sacrificed) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new RecklessAbandon()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player2, 0,
                harness.getPermanentId(player1, "False Prophet"), sacrificed.getId());
        harness.passBothPriorities();
    }
}
