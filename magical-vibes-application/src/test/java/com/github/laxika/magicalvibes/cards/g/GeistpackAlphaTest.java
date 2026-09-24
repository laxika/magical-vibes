package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GeistpackAlpha.class, Forest.class, GrizzlyBears.class, Negate.class, WrathOfGod.class})
class GeistpackAlphaTest extends BaseCardTest {

    @Test
    void seeksPermanentWithManaValueEqualToLandsControlledWhenItDies() {
        Forest firstLand = new Forest();
        Forest secondLand = new Forest();
        GrizzlyBears sought = new GrizzlyBears();
        Negate nonPermanent = new Negate();
        harness.addToBattlefield(player1, firstLand);
        harness.addToBattlefield(player1, secondLand);
        harness.setLibrary(player1, List.of(nonPermanent, sought));
        harness.addToBattlefield(player1, new GeistpackAlpha());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonPermanent);
    }

    @Test
    void doesNotSeekPermanentWithDifferentManaValue() {
        harness.addToBattlefield(player1, new Forest());
        Card wrongManaValue = new GrizzlyBears();
        harness.setLibrary(player1, List.of(wrongManaValue));
        Permanent geistpack = harness.addToBattlefieldAndReturn(player1, new GeistpackAlpha());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(wrongManaValue);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(geistpack.getCard());
    }
}
