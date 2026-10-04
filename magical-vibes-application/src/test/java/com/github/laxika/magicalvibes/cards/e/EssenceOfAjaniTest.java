package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceOfAjani.class, GrizzlyBears.class, Counterspell.class})
class EssenceOfAjaniTest extends BaseCardTest {

    @Test
    void resolvesIntoCommandZoneAndGainsLifeWhenControllerCastsASpell() {
        Card essence = new EssenceOfAjani();
        harness.castFromHand(player1, essence, "{2}{W}");
        resolveAllTriggers();

        assertThat(gd.playerCommandZones.get(player1.getId())).contains(essence);
        harness.assertNotInGraveyard(player1, "Essence of Ajani");
        harness.assertLife(player1, 20);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertLife(player1, 21);
    }

    @Test
    void opponentCastingASpellDoesNotTriggerYourEmblem() {
        harness.castFromHand(player1, new EssenceOfAjani(), "{2}{W}");
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new EssenceOfAjani(), "{2}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void eachResolvedEmblemTriggersForLaterSpells() {
        Card first = new EssenceOfAjani();
        Card second = new EssenceOfAjani();
        Card third = new EssenceOfAjani();
        harness.castFromHand(player1, first, "{2}{W}");
        resolveAllTriggers();

        harness.castFromHand(player1, second, "{2}{W}");
        resolveAllTriggers();
        harness.assertLife(player1, 21);

        harness.castFromHand(player1, third, "{2}{W}");
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(first, second, third);
    }

    @Test
    void counteredEmblemGoesToGraveyardAndDoesNotGainLifeForLaterSpells() {
        Card essence = new EssenceOfAjani();
        harness.castFromHand(player1, essence, "{2}{W}");

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, essence.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Essence of Ajani");
        assertThat(gd.playerCommandZones.get(player1.getId())).doesNotContain(essence);

        harness.castFromHand(player1, new EssenceOfAjani(), "{2}{W}");
        resolveAllTriggers();
        harness.assertLife(player1, 20);
    }

    @Test
    void gainsLifeEvenWhenTheTriggeringSpellIsCountered() {
        Card first = new EssenceOfAjani();
        harness.castFromHand(player1, first, "{2}{W}");
        resolveAllTriggers();

        Card second = new EssenceOfAjani();
        harness.castFromHand(player1, second, "{2}{W}");
        harness.assertLife(player1, 20);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, second.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Essence of Ajani");
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(first);
    }
}
