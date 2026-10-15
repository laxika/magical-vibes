package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.StinkweedImp;
import com.github.laxika.magicalvibes.cards.u.UlamogsCrusher;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseAugur.class, UlamogsCrusher.class, Forest.class, StinkweedImp.class})
class CorpseAugurTest extends BaseCardTest {

    @Test
    @DisplayName("When Corpse Augur dies, it draws and loses life for creature cards in the target player's graveyard")
    void diesDrawsAndLosesLifeForTargetPlayersCreatureCards() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new UlamogsCrusher(), new Forest(), new UlamogsCrusher()));
        harness.setLife(player1, 20);

        killAugurInCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Corpse Augur");
    }

    @Test
    @DisplayName("Targeting yourself counts Corpse Augur in your graveyard")
    void selfTargetIncludesAugur() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new UlamogsCrusher(), new Forest()));

        killAugurInCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A target graveyard without creature cards causes no draw or life loss")
    void noCreatureCardsMeansZero() {
        Forest topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setGraveyard(player2, List.of(new Forest()));

        killAugurInCombat();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The creature count is determined on resolution rather than when the target is chosen")
    void countsGraveyardAtResolution() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new UlamogsCrusher()));

        killAugurInCombat();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setGraveyard(player2, List.of(new UlamogsCrusher(), new UlamogsCrusher(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Removing Corpse Augur before resolution excludes it from the creature count")
    void doesNotCountAugurAfterItLeavesGraveyard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new UlamogsCrusher()));

        killAugurInCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.setGraveyard(player1, List.of(new UlamogsCrusher()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Dredging during the draw does not change X for the life loss")
    void dredgeDoesNotChangeLifeLossAmount() {
        StinkweedImp imp = new StinkweedImp();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(imp));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));

        killAugurInCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(imp);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    private void killAugurInCombat() {
        Permanent augur = addCreatureReady(player1, new CorpseAugur());
        augur.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UlamogsCrusher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();
    }
}
