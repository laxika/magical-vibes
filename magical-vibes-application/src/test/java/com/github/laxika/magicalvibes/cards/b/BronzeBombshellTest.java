package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ActOfAggression;
import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BronzeBombshell.class, ActOfTreason.class, ActOfAggression.class})
class BronzeBombshellTest extends BaseCardTest {

    @Test
    void doesNotTriggerWhileItsOwnerControlsIt() {
        Card bronzeBombshell = ownedBronzeBombshell(player1);
        harness.setHand(player1, List.of(bronzeBombshell));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bronze Bombshell");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonownerControlsItAndTakesSevenDamageWhenItResolves() {
        Permanent bronzeBombshell = castBronzeBombshell(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castSorcery(player2, 0, bronzeBombshell.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Bronze Bombshell");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bronze Bombshell");
        harness.assertNotOnBattlefield(player2, "Bronze Bombshell");
        harness.assertInGraveyard(player1, "Bronze Bombshell");
        harness.assertLife(player2, 13);
    }

    @Test
    void doesNotSacrificeOrDamageIfControlChangesBeforeResolution() {
        Permanent bronzeBombshell = castBronzeBombshell(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castSorcery(player2, 0, bronzeBombshell.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castInstant(player1, 0, bronzeBombshell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bronze Bombshell");
        harness.assertNotOnBattlefield(player2, "Bronze Bombshell");
        harness.assertNotInGraveyard(player1, "Bronze Bombshell");
        harness.assertLife(player2, 20);
    }

    private Permanent castBronzeBombshell(Player player) {
        Card bronzeBombshell = ownedBronzeBombshell(player);
        harness.setHand(player, List.of(bronzeBombshell));
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "Bronze Bombshell");
    }

    private Card ownedBronzeBombshell(Player owner) {
        Card bronzeBombshell = new BronzeBombshell();
        bronzeBombshell.setOwnerId(owner.getId());
        return bronzeBombshell;
    }
}
