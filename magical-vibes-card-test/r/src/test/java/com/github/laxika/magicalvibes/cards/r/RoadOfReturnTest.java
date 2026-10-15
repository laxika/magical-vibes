package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AinokSurvivalist;
import com.github.laxika.magicalvibes.cards.j.JungleHollow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoadOfReturn.class, AinokSurvivalist.class, RayamiFirstOfTheFallen.class, JungleHollow.class})
class RoadOfReturnTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentFromGraveyardToHand() {
        Card permanent = new AinokSurvivalist();
        harness.setGraveyard(player1, List.of(permanent));
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ainok Survivalist");
        harness.assertNotInGraveyard(player1, "Ainok Survivalist");
    }

    @Test
    void commanderModePutsCommanderIntoHand() {
        Card commander = new RayamiFirstOfTheFallen();
        gd.playerCommandZones.get(player1.getId()).add(commander);
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerCommandZones.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    void entwineResolvesBothModesAndPaysAdditionalMana() {
        Card permanent = new AinokSurvivalist();
        Card commander = new RayamiFirstOfTheFallen();
        harness.setGraveyard(player1, List.of(permanent));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        prepareCard(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(), null);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ainok Survivalist");
        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void graveyardModeExcludesNonPermanentCards() {
        Card nonPermanent = new RoadOfReturn();
        Card permanent = new AinokSurvivalist();
        harness.setGraveyard(player1, List.of(nonPermanent, permanent));
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(permanent.getId());
    }

    @Test
    void entwineRequiresAdditionalMana() {
        harness.setGraveyard(player1, List.of(new AinokSurvivalist()));
        prepareCard(2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardModeRequiresOneTarget() {
        Card permanent = new AinokSurvivalist();
        harness.setGraveyard(player1, List.of(permanent));
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void graveyardModeCannotBeCastWithoutALegalTarget() {
        harness.setGraveyard(player1, List.of(new RoadOfReturn()));
        prepareCard(2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entwineCannotBeCastWithoutALegalGraveyardTarget() {
        gd.playerCommandZones.get(player1.getId()).add(new RayamiFirstOfTheFallen());
        prepareCard(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void entwineDoesNotReturnCommanderWhenItsOnlyTargetBecomesIllegal() {
        Card permanent = new AinokSurvivalist();
        Card commander = new RayamiFirstOfTheFallen();
        harness.setGraveyard(player1, List.of(permanent));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        prepareCard(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(), null);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(permanent));
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player1.getId())).contains(commander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(commander, permanent);
        harness.assertInGraveyard(player1, "Road of Return");
    }

    @Test
    void commanderModeWithEmptyCommandZoneDoesNotReturnOpponentsCommander() {
        Card commander = new RayamiFirstOfTheFallen();
        gd.playerCommandZones.get(player2.getId()).add(commander);
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player2.getId())).contains(commander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(commander);
        harness.assertInGraveyard(player1, "Road of Return");
    }

    @Test
    void graveyardModeReturnsLandCardsWithoutReturningOpponentsCards() {
        Card land = new JungleHollow();
        Card opponentsPermanent = new AinokSurvivalist();
        harness.setGraveyard(player1, List.of(land));
        harness.setGraveyard(player2, List.of(opponentsPermanent));
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentsPermanent);
    }

    private void prepareCard(int greenMana) {
        harness.setHand(player1, List.of(new RoadOfReturn()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
    }
}
