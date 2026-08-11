package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LluwenImperfectNaturalistTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills four and may put a milled creature or land on top")
    void etbMillsAndMayPutEligibleCardOnTop() {
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        Mountain otherLand = new Mountain();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(creature, land, otherLand, new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LluwenImperfectNaturalist()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("ETB only offers creature or land cards from the milled cards")
    void etbOnlyOffersCreatureOrLandCards() {
        GiantGrowth instant = new GiantGrowth();
        Forest land = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(instant, land, new Mountain(), new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LluwenImperfectNaturalist()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant);
    }

    @Test
    @DisplayName("Discarding a land creates one Worm token per land in the graveyard")
    void activationCreatesWormsForLandsInGraveyard() {
        Permanent lluwen = new Permanent(new LluwenImperfectNaturalist());
        lluwen.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(lluwen);
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        harness.setGraveyard(player1, List.of(new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        long wormCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
        assertThat(wormCount).isEqualTo(3);
    }
}
