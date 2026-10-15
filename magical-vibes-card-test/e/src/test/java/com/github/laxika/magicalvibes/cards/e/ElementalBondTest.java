package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.r.RhoxMaulers;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.u.UnholyHunger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElementalBond.class, BoggartBrute.class, TimberpackWolf.class,
        RhoxMaulers.class, UnholyHunger.class})
class ElementalBondTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when a creature with power 3 or greater enters under controller's control")
    void drawsWhenPower3OrGreaterCreatureEnters() {
        harness.addToBattlefield(player1, new ElementalBond());

        harness.castFromHand(player1, new BoggartBrute(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when a creature with power less than 3 enters")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new ElementalBond());

        harness.castFromHand(player1, new TimberpackWolf(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent's creature with power 3 or greater enters")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new ElementalBond());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new BoggartBrute(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsForCreatureAbovePowerThreshold() {
        harness.addToBattlefield(player1, new ElementalBond());
        var drawnCard = new TimberpackWolf();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new RhoxMaulers(), "{4}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void checksStaticBoostsAtEntryAndDoesNotRecheckPowerOnResolution() {
        harness.addToBattlefield(player1, new ElementalBond());
        var otherWolf = harness.addToBattlefieldAndReturn(player1, new TimberpackWolf());
        var drawnCard = new BoggartBrute();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new TimberpackWolf(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new UnholyHunger()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, otherWolf.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherWolf);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsEvenIfEnteringCreatureLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new ElementalBond());
        var drawnCard = new TimberpackWolf();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new BoggartBrute(), "{2}{R}");
        harness.passBothPriorities();
        var creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof BoggartBrute)
                .findFirst().orElseThrow();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new UnholyHunger()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).isEmpty();
    }
}
