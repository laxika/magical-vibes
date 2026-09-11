package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowsVerdict.class, GrizzlyBears.class, HillGiant.class, Island.class,
        JaceBeleren.class, Shock.class})
class ShadowsVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles qualifying creatures and planeswalkers from the battlefield and graveyards")
    void exilesCreaturesAndPlaneswalkersWithManaValueThreeOrLess() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownPlaneswalker = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
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
        harness.setHand(player1, List.of(verdict));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
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
}
