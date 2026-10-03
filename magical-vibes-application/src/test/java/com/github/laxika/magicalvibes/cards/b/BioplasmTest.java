package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
import com.github.laxika.magicalvibes.cards.m.Mortivore;
import com.github.laxika.magicalvibes.cards.s.SkarrgTheRagePits;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Bioplasm.class, GhorClanSavage.class, SkarrgTheRagePits.class, Mortivore.class})
class BioplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and gets its power and toughness until end of turn")
    void exiledCreatureBoostsBioplasm() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        GhorClanSavage topCard = new GhorClanSavage();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiling a noncreature card does not boost Bioplasm")
    void exiledNoncreatureDoesNotBoostBioplasm() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        SkarrgTheRagePits topCard = new SkarrgTheRagePits();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Exiles only the top card of a multi-card library")
    void exilesOnlyTopLibraryCard() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        GhorClanSavage topCard = new GhorClanSavage();
        SkarrgTheRagePits remainingCard = new SkarrgTheRagePits();
        harness.setLibrary(player1, List.of(topCard, remainingCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(7);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        harness.setLibrary(player1, List.of(new GhorClanSavage()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(4);
    }

    @Test
    @DisplayName("An empty library does not boost Bioplasm")
    void emptyLibraryDoesNotBoostBioplasm() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(4);
    }

    @Test
    @CardUsed({Bioplasm.class, GhorClanSavage.class, SkarrgTheRagePits.class, Mortivore.class})
    @DisplayName("Exiled creature characteristic-defining abilities determine the boost")
    void exiledCreatureUsesPowerAndToughnessDefinedInExile() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        Mortivore topCard = new Mortivore();
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player1, List.of(new GhorClanSavage(), new SkarrgTheRagePits()));
        harness.setGraveyard(player2, List.of(new GhorClanSavage()));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(6);
    }

    @Test
    @DisplayName("The attack trigger still exiles a card after Bioplasm leaves the battlefield")
    void triggerExilesCardAfterSourceLeavesBattlefield() {
        Permanent bioplasm = addCreatureReady(player1, new Bioplasm());
        GhorClanSavage topCard = new GhorClanSavage();
        harness.setLibrary(player1, List.of(topCard));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(bioplasm);
        gd.playerGraveyards.get(player1.getId()).add(bioplasm.getCard());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Bioplasm exiles from its controller's library")
    void opponentBioplasmUsesItsControllersLibrary() {
        Permanent bioplasm = addCreatureReady(player2, new Bioplasm());
        GhorClanSavage topCard = new GhorClanSavage();
        SkarrgTheRagePits otherLibraryCard = new SkarrgTheRagePits();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(otherLibraryCard));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(otherLibraryCard);
        assertThat(gqs.getEffectivePower(gd, bioplasm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bioplasm)).isEqualTo(7);
    }
}
