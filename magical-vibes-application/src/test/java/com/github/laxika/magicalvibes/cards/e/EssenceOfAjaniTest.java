package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceOfAjani.class, GrizzlyBears.class})
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
}
