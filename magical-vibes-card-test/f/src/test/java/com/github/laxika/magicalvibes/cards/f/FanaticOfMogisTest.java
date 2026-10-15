package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Fanatic of Mogis")
@CardUsed({FanaticOfMogis.class, LightningStrike.class})
class FanaticOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals damage to each opponent equal to red devotion")
    void etbDealsDamageEqualToRedDevotion() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new FanaticOfMogis());
        harness.setHand(player1, List.of(new FanaticOfMogis()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only controlled permanents contribute devotion, and the controller takes no damage")
    void excludesOpponentsPermanentsAndCardsInHand() {
        harness.addToBattlefield(player2, new FanaticOfMogis());
        harness.setHand(player1, List.of(new FanaticOfMogis(), new FanaticOfMogis()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the source before resolution leaves zero devotion and deals no damage")
    void sourceRemovedBeforeResolutionWithZeroDevotion() {
        harness.setHand(player1, List.of(new FanaticOfMogis()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Fanatic of Mogis"));
        harness.assertInGraveyard(player1, "Fanatic of Mogis");
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger survives source removal and counts remaining devotion at resolution")
    void sourceRemovedBeforeResolutionWithRemainingDevotion() {
        var otherFanatic = harness.addToBattlefieldAndReturn(player1, new FanaticOfMogis());
        harness.setHand(player1, List.of(new FanaticOfMogis()));
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(otherFanatic.getId()))
                .findFirst().orElseThrow();
        harness.castAndResolveInstant(player2, 0, source.getId());
        harness.assertInGraveyard(player1, "Fanatic of Mogis");
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(gd.stack).isEmpty();
    }
}
