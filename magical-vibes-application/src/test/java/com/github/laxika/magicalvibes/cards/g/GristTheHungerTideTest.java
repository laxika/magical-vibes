package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GristTheHungerTide.class, Forest.class, GrizzlyBears.class})
class GristTheHungerTideTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates an Insect, mills, and repeats after milling an Insect")
    void plusOneRepeatsWhenAnInsectIsMilled() {
        Permanent grist = addReadyGrist(3);
        harness.setLibrary(player1, List.of(new GristTheHungerTide(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countTokens(player1)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 sacrifices a creature and destroys a target creature")
    void minusTwoSacrificesAndDestroys() {
        Permanent grist = addReadyGrist(3);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(grist.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("-5 makes each opponent lose life for creature cards in the controller's graveyard")
    void minusFiveCountsCreatureCardsInGraveyard() {
        addReadyGrist(5);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Grist is in the graveyard by the time the ability resolves and its static ability
        // makes it a creature card there as well.
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    private Permanent addReadyGrist(int loyalty) {
        Permanent perm = new Permanent(new GristTheHungerTide());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private long countTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .map(Permanent::getCard)
                .filter(Card::isToken)
                .count();
    }
}
