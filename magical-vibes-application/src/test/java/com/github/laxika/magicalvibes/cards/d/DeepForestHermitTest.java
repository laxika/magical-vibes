package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepForestHermit.class, GiantCockroach.class})
class DeepForestHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates four Squirrels that get +1/+1")
    void enteringCreatesFourBuffedSquirrels() {
        castAndResolveHermit();

        List<Permanent> squirrels = squirrelTokens(player1);
        assertThat(squirrels).hasSize(4);
        for (Permanent squirrel : squirrels) {
            assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("The static ability buffs Squirrels controlled by any player")
    void buffsSquirrelsControlledByAnyPlayer() {
        harness.addToBattlefield(player1, new DeepForestHermit());
        Permanent ownSquirrel = harness.addToBattlefieldAndReturn(player1, squirrelToken());
        Permanent opponentSquirrel = harness.addToBattlefieldAndReturn(player2, squirrelToken());
        Permanent nonSquirrel = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());

        assertThat(gqs.getEffectivePower(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentSquirrel)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonSquirrel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, nonSquirrel)).isEqualTo(2);
    }

    @Test
    @DisplayName("Vanishing removes one time counter at each upkeep and sacrifices on the last")
    void vanishingRemovesCountersAndThenSacrifices() {
        Permanent hermit = addCreatureReady(player1, new DeepForestHermit());
        hermit.setCounterCount(CounterType.TIME, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(hermit.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Deep Forest Hermit");
        harness.assertInGraveyard(player1, "Deep Forest Hermit");
    }

    private void castAndResolveHermit() {
        harness.castFromHand(player1, new DeepForestHermit(), "{3}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deep Forest Hermit");
    }

    private List<Permanent> squirrelTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SQUIRREL))
                .toList();
    }

    private Card squirrelToken() {
        Card card = new Card();
        card.setName("Squirrel");
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.SQUIRREL));
        card.setToken(true);
        return card;
    }
}
