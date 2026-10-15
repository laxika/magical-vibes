package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.n.NahiriHeirOfTheAncients;
import com.github.laxika.magicalvibes.cards.r.RisenRiptide;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowsVerdict.class, GrizzlyBears.class, HillGiant.class, Island.class,
        JaceBeleren.class, Shock.class, NahiriHeirOfTheAncients.class, RisenRiptide.class})
class ShadowsVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles qualifying creatures and planeswalkers from the battlefield and graveyards")
    void exilesCreaturesAndPlaneswalkersWithManaValueThreeOrLess() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownPlaneswalker = harness.enterBattlefieldAndReturn(player1, new JaceBeleren());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLargeCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        Card ownGraveyardCreature = new GrizzlyBears();
        Card ownGraveyardPlaneswalker = new JaceBeleren();
        Card ownGraveyardInstant = new Shock();
        Card opponentGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardLand = new Island();
        harness.setGraveyard(player1, List.of(ownGraveyardCreature, ownGraveyardPlaneswalker, ownGraveyardInstant));
        harness.setGraveyard(player2, List.of(opponentGraveyardCreature, opponentGraveyardLand));

        ShadowsVerdict verdict = new ShadowsVerdict();
        harness.castFromHand(player1, verdict, "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentLargeCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(ownCreature.getCard(), ownPlaneswalker.getCard(), ownGraveyardCreature,
                        ownGraveyardPlaneswalker);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(opponentCreature.getCard(), opponentGraveyardCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGraveyardInstant, verdict);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardLand);
    }

    @Test
    @DisplayName("Exiles mana-value-three creatures but preserves mana-value-four planeswalkers in both zones")
    void respectsManaValueBoundaryInBothZones() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RisenRiptide());
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player1, new NahiriHeirOfTheAncients());
        Card graveyardCreature = new RisenRiptide();
        Card graveyardPlaneswalker = new NahiriHeirOfTheAncients();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setGraveyard(player2, List.of(graveyardPlaneswalker));

        ShadowsVerdict verdict = new ShadowsVerdict();
        harness.castFromHand(player1, verdict, "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(planeswalker);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(verdict);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardPlaneswalker);
    }

    @Test
    @DisplayName("Exiles qualifying graveyard cards even when the battlefield is empty")
    void exilesGraveyardCardsWithEmptyBattlefield() {
        Card ownCreature = new RisenRiptide();
        Card opponentCreature = new RisenRiptide();
        Card ownPlaneswalker = new NahiriHeirOfTheAncients();
        harness.setGraveyard(player1, List.of(ownCreature, ownPlaneswalker));
        harness.setGraveyard(player2, List.of(opponentCreature));

        ShadowsVerdict verdict = new ShadowsVerdict();
        harness.castFromHand(player1, verdict, "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownPlaneswalker, verdict);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolves when neither battlefield nor graveyards contain qualifying cards")
    void resolvesWithoutQualifyingCards() {
        Permanent planeswalker = harness.enterBattlefieldAndReturn(player2, new NahiriHeirOfTheAncients());
        Card graveyardPlaneswalker = new NahiriHeirOfTheAncients();
        harness.setGraveyard(player2, List.of(graveyardPlaneswalker));

        ShadowsVerdict verdict = new ShadowsVerdict();
        harness.castFromHand(player1, verdict, "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(planeswalker);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(verdict);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardPlaneswalker);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
