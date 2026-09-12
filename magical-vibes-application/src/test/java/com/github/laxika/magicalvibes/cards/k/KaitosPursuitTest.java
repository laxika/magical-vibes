package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.cards.w.WalkerOfSecretWays;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaitosPursuit.class, DeathcultRogue.class, GrizzlyBears.class, Peek.class, WalkerOfSecretWays.class})
class KaitosPursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards and your Ninjas and Rogues gain menace")
    void discardsAndGrantsMenaceToNinjasAndRogues() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new WalkerOfSecretWays());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new DeathcultRogue());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentNinja = harness.addToBattlefieldAndReturn(player2, new WalkerOfSecretWays());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rogue, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonmatching, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentNinja, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace lasts until end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new WalkerOfSecretWays());
        harness.setHand(player2, new ArrayList<>(List.of(new Peek(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a player")
    void cannotTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkerOfSecretWays());
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }
}
