package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfWestwardVoyager.class, ColossalDreadmaw.class, GrizzlyBears.class, Forest.class})
class GandalfWestwardVoyagerTest extends BaseCardTest {

    @Test
    void matchingRevealedCardCopiesPermanentSpellAndEachOpponentDraws() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card revealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.setHand(player2, List.of());
        addColossalDreadmawMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> dreadmaws = findPermanents(player1, "Colossal Dreadmaw");
        assertThat(dreadmaws).hasSize(2);
        assertThat(dreadmaws.stream().filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void nonmatchingRevealedCardsMakeYouDrawInsteadOfCopying() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card drawn = new GrizzlyBears();
        Card revealed = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        addColossalDreadmawMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Colossal Dreadmaw")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void doesNotTriggerForSpellWithManaValueBelowFive() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }

    private void addColossalDreadmawMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
