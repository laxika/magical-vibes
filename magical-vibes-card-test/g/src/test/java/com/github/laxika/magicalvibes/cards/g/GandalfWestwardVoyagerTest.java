package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.a.ArcaneDenial;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GandalfWestwardVoyager.class, ColossalDreadmaw.class, GrizzlyBears.class, Forest.class,
        ArcaneDenial.class, Hurricane.class, LavaAxe.class})
class GandalfWestwardVoyagerTest extends BaseCardTest {

    @Test
    void matchingRevealedCardCopiesPermanentSpellAndEachOpponentDraws() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card revealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player2, List.of());
        harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");
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
        harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }

    @Test
    void emptyOpponentLibraryMakesControllerDraw() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new ColossalDreadmaw(), "{4}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(findPermanents(player1, "Colossal Dreadmaw")).hasSize(1);
    }

    @Test
    void chosenXCountsTowardThresholdAndIsPreservedByCopy() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card revealed = new Hurricane();
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void fiveManaSpellCopyCanKeepOriginalTarget() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card revealed = new Hurricane();
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void fiveManaSpellCopyCanChooseNewTarget() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card revealed = new Hurricane();
        harness.setLibrary(player2, List.of(revealed));
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void counteredSpellStillMakesControllerDrawWhenRevealedTypeDoesNotMatch() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card drawn = new Forest();
        Card revealed = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(revealed));
        Card spell = new ColossalDreadmaw();
        harness.castFromHand(player1, spell, "{4}{G}{G}");
        harness.setHand(player2, List.of(new ArcaneDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(revealed);
        assertThat(findPermanents(player1, "Colossal Dreadmaw")).isEmpty();
    }

    @Test
    void counteredSpellStillMakesOpponentDrawWhenRevealedTypeMatches() {
        harness.addToBattlefield(player1, new GandalfWestwardVoyager());
        Card revealed = new GrizzlyBears();
        harness.setLibrary(player2, List.of(revealed));
        Card spell = new ColossalDreadmaw();
        harness.castFromHand(player1, spell, "{4}{G}{G}");
        harness.setHand(player2, List.of(new ArcaneDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        List<Permanent> copies = findPermanents(player1, "Colossal Dreadmaw");
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getCard().isToken()).isTrue();
    }
}
