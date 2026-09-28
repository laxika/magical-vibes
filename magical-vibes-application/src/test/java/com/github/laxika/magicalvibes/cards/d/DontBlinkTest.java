package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EternalScourge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DontBlink.class, EternalScourge.class, GrizzlyBears.class})
class DontBlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles a creature entering from exile into its owner's library")
    void shufflesCreatureEnteringFromExile() {
        castDontBlink();

        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player2, List.of());
        harness.setExile(player2, List.of(bear));
        gd.removeFromExile(bear.getId());
        Permanent entering = new Permanent(bear, Zone.EXILE);
        harness.inMutationScope(() -> harness.getBattlefieldEntryService()
                .putPermanentOntoBattlefield(gd, player2.getId(), entering));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bear);
    }

    @Test
    @DisplayName("Shuffles a creature cast from exile into its owner's library")
    void shufflesCreatureCastFromExile() {
        castDontBlink();

        EternalScourge scourge = new EternalScourge();
        harness.setLibrary(player2, List.of());
        harness.setExile(player2, List.of(scourge));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castFromExile(player2, scourge.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Eternal Scourge");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(scourge);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(scourge);
    }

    @Test
    @DisplayName("Cycling draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new DontBlink()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Don't Blink");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void castDontBlink() {
        harness.setHand(player1, List.of(new DontBlink()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
