package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DauntlessCathar;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.j.JustTheWind;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManicScribe.class, Forest.class, DauntlessCathar.class, JustTheWind.class,
        DeadWeight.class})
class ManicScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and makes each opponent mill three cards")
    void millsEachOpponentOnEnter() {
        harness.setLibrary(player1, library(5));
        harness.setLibrary(player2, library(5));
        harness.setHand(player1, List.of(new ManicScribe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Delirium mills the opponent whose upkeep it is")
    void millsOpponentOnUpkeepWithDelirium() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Does not mill on an opponent's upkeep without delirium")
    void doesNotMillWithoutDelirium() {
        harness.setLibrary(player2, library(5));
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Does not trigger on its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Rechecks delirium when the triggered ability resolves")
    void rechecksDeliriumAtResolution() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @CardUsed({WitchbaneOrb.class})
    @DisplayName("Upkeep milling does not target and ignores the opponent's hexproof")
    void upkeepMillsOpponentWithHexproof() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());
        harness.addToBattlefield(player2, new WitchbaneOrb());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Gaining delirium after upkeep begins does not create a trigger")
    void gainingDeliriumAfterUpkeepDoesNotTrigger() {
        harness.setLibrary(player2, library(5));
        harness.setGraveyard(player1, List.of(
                new DauntlessCathar(), new Forest(), new JustTheWind()));
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        setDelirium();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Four cards of only three types do not satisfy delirium")
    void countsDistinctTypesInsteadOfCards() {
        harness.setLibrary(player2, library(5));
        harness.setGraveyard(player1, List.of(
                new DauntlessCathar(), new Forest(), new Forest(), new JustTheWind()));
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Upkeep ability still mills after Manic Scribe leaves the battlefield")
    void upkeepTriggerSurvivesSourceLeaving() {
        harness.setLibrary(player2, library(5));
        setDelirium();
        harness.addToBattlefield(player1, new ManicScribe());

        advanceToUpkeep(player2);
        harness.setHand(player2, List.of(new JustTheWind()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0,
                gd.playerBattlefields.get(player1.getId()).getFirst().getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Enter ability mills all remaining cards when the library has fewer than three")
    void millsShortLibraryOnEnter() {
        harness.setLibrary(player2, library(2));
        harness.setHand(player1, List.of(new ManicScribe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new DauntlessCathar(), new Forest(), new JustTheWind(), new DeadWeight()));
    }

    private List<Card> library(int size) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
