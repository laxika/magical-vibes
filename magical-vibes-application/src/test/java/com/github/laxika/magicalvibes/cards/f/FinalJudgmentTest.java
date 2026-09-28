package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FinalJudgment.class, GnarledMass.class, MirrorGallery.class})
class FinalJudgmentTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles every creature on both battlefields")
    void exilesAllCreatures() {
        harness.addToBattlefield(player1, new GnarledMass());
        harness.addToBattlefield(player2, new GnarledMass());

        harness.castFromHand(player1, new FinalJudgment(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(exiledCard -> exiledCard.card().getName())
                .containsExactlyInAnyOrder("Gnarled Mass", "Gnarled Mass");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Gnarled Mass"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Gnarled Mass"));
    }

    @Test
    @DisplayName("Leaves noncreature permanents on the battlefield")
    void leavesNoncreaturePermanents() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player2, new GnarledMass());

        harness.castFromHand(player1, new FinalJudgment(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Mirror Gallery");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolves with no creatures on the battlefield")
    void resolvesWithNothingToHit() {
        harness.castFromHand(player1, new FinalJudgment(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Final Judgment");
    }

    @Test
    @DisplayName("Leaves creature cards in hand and graveyards alone")
    void doesNotExileCreatureCardsOutsideTheBattlefield() {
        GnarledMass creatureInHand = new GnarledMass();
        GnarledMass creatureInGraveyard = new GnarledMass();
        harness.setHand(player2, List.of(creatureInHand));
        harness.setGraveyard(player2, List.of(creatureInGraveyard));
        harness.addToBattlefield(player1, new GnarledMass());

        harness.castFromHand(player1, new FinalJudgment(), "{4}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.exiledCards)
                .extracting(exiledCard -> exiledCard.card().getName())
                .containsExactly("Gnarled Mass");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creatureInHand);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creatureInGraveyard);
    }
}
