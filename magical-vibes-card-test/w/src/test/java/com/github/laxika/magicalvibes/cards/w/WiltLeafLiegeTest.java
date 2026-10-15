package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CinderhazeWretch;
import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WiltLeafLiege.class, Distress.class, EliteVanguard.class, GrizzlyBears.class,
        HillGiant.class, CinderhazeWretch.class})
class WiltLeafLiegeTest extends BaseCardTest {

    @Test
    @DisplayName("Other green creatures you control get +1/+1")
    void buffsOwnGreenCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent vanguard = addCreatureReady(player1, new EliteVanguard());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vanguard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures that are neither green nor white are not buffed")
    void doesNotBuffOtherColors() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent's green creatures are not buffed")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent liege = harness.addToBattlefieldAndReturn(player1, new WiltLeafLiege());

        assertThat(gqs.getEffectivePower(gd, liege)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, liege)).isEqualTo(4);
    }

    @Test
    @DisplayName("A green-white creature gets both bonuses (+2/+2)")
    void greenWhiteCreatureGetsBothBonuses() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        // A second Liege is both green and white, so it gets +1/+1 twice from the first.
        Permanent secondLiege = addCreatureReady(player1, new WiltLeafLiege());

        assertThat(gqs.getEffectivePower(gd, secondLiege)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, secondLiege)).isEqualTo(6);
    }

    @Test
    @DisplayName("Enters battlefield when discarded by opponent via Distress")
    void entersBattlefieldWhenDiscardedByOpponent() {
        harness.setHand(player2, new ArrayList<>(List.of(new WiltLeafLiege())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player1 chooses Wilt-Leaf Liege from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player2, "Wilt-Leaf Liege");
        harness.assertNotInGraveyard(player2, "Wilt-Leaf Liege");
    }

    @Test
    @DisplayName("An opponent's green-white creature gets neither bonus")
    void doesNotBuffOpponentGreenWhiteCreatures() {
        harness.addToBattlefield(player1, new WiltLeafLiege());
        Permanent opponentLiege = harness.addToBattlefieldAndReturn(player2, new WiltLeafLiege());

        assertThat(gqs.getEffectivePower(gd, opponentLiege)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentLiege)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent-controlled activated ability puts the discarded Liege onto the battlefield")
    void entersBattlefieldWhenDiscardedByOpponentAbility() {
        addCreatureReady(player1, new CinderhazeWretch());
        Permanent firstLiege = harness.addToBattlefieldAndReturn(player2, new WiltLeafLiege());
        harness.setHand(player2, List.of(new WiltLeafLiege()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(countPermanents(player2, "Wilt-Leaf Liege")).isEqualTo(2);
        harness.assertNotInHand(player2, "Wilt-Leaf Liege");
        harness.assertNotInGraveyard(player2, "Wilt-Leaf Liege");
        assertThat(gqs.getEffectivePower(gd, firstLiege)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, firstLiege)).isEqualTo(6);
    }

    @Test
    @DisplayName("Discarding to your own activated ability does not put Liege onto the battlefield")
    void ownAbilityDoesNotReplaceDiscard() {
        addCreatureReady(player1, new CinderhazeWretch());
        harness.setHand(player1, List.of(new WiltLeafLiege()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Wilt-Leaf Liege");
        harness.assertNotOnBattlefield(player1, "Wilt-Leaf Liege");
        harness.assertNotInHand(player1, "Wilt-Leaf Liege");
    }

    @Test
    @DisplayName("Discarding to your own Distress does not put Liege onto the battlefield")
    void ownSpellDoesNotReplaceDiscard() {
        harness.setHand(player1, List.of(new Distress(), new WiltLeafLiege()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Wilt-Leaf Liege");
        harness.assertNotOnBattlefield(player1, "Wilt-Leaf Liege");
        harness.assertNotInHand(player1, "Wilt-Leaf Liege");
    }
}
