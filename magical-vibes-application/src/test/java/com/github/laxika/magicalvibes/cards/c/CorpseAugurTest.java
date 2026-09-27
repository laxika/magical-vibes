package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpseAugur.class, GrizzlyBears.class, Forest.class})
class CorpseAugurTest extends BaseCardTest {

    @Test
    @DisplayName("When Corpse Augur dies, it draws and loses life for creature cards in the target player's graveyard")
    void diesDrawsAndLosesLifeForTargetPlayersCreatureCards() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        Permanent augur = addCreatureReady(player1, new CorpseAugur());
        augur.setAttacking(true);

        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(3);
        blockerCard.setToughness(3);
        Permanent blocker = addCreatureReady(player2, blockerCard);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Corpse Augur");
    }
}
