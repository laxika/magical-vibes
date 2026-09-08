package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindmoil.class, Forest.class, GrizzlyBears.class, LightningBolt.class, Mountain.class})
class MindmoilTest extends BaseCardTest {

    @Test
    @DisplayName("After you cast a spell, you put your hand on the bottom of your library and draw that many")
    void replacesHandAfterCastingSpell() {
        Forest drawnCard = new Forest();
        Mountain nextLibraryCard = new Mountain();
        GrizzlyBears remainingHandCard = new GrizzlyBears();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(new LightningBolt(), remainingHandCard));
        harness.setLibrary(player1, List.of(drawnCard, nextLibraryCard));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextLibraryCard, remainingHandCard);
    }

    @Test
    @DisplayName("An opponent's spell does not trigger Mindmoil")
    void opponentSpellDoesNotTrigger() {
        Forest player1LibraryCard = new Forest();
        LightningBolt opponentSpell = new LightningBolt();
        GrizzlyBears player1HandCard = new GrizzlyBears();

        harness.addToBattlefield(player1, new Mindmoil());
        harness.setHand(player1, List.of(player1HandCard));
        harness.setLibrary(player1, List.of(player1LibraryCard));
        harness.setHand(player2, List.of(opponentSpell));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1HandCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(player1LibraryCard);
    }
}
