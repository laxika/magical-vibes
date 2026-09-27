package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrosanColossus;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhulodokVoidGorger.class, DarksteelColossus.class, GrizzlyBears.class,
        HillGiant.class, KrosanColossus.class, WurmcoilEngine.class})
class ZhulodokVoidGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("A colorless spell with mana value 7 or greater gets cascade twice")
    void qualifyingColorlessSpellGetsCascadeTwice() {
        setupWithZhulodok(player1);
        harness.setLibrary(player1, List.of(new HillGiant(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DarksteelColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Hill Giant");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).extracting("name").containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("A colored spell does not get cascade")
    void coloredSpellDoesNotGetCascade() {
        setupWithZhulodok(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new KrosanColossus()));
        harness.addMana(player1, ManaColor.GREEN, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A colorless spell below mana value 7 does not get cascade")
    void lowManaValueSpellDoesNotGetCascade() {
        setupWithZhulodok(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new WurmcoilEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Zhulodok does not grant cascade to an opponent's spell")
    void opponentSpellDoesNotGetCascade() {
        setupWithZhulodok(player1);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new DarksteelColossus()));
        harness.addMana(player2, ManaColor.COLORLESS, 11);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    private void setupWithZhulodok(com.github.laxika.magicalvibes.model.Player controller) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(controller);
        harness.addToBattlefield(controller, new ZhulodokVoidGorger());
    }
}
