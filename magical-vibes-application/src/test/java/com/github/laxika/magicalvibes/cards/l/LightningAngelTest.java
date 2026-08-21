package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningAngel.class, GrizzlyBears.class})
class LightningAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying, vigilance, and haste on the battlefield")
    void hasPrintedKeywords() {
        Permanent angel = addCreatureReady(player1, new LightningAngel());

        assertThat(angel.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(angel.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(angel.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new LightningAngel());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Vigilance keeps it untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent angel = addCreatureReady(player1, new LightningAngel());

        declareAttackers(player1, List.of(0));

        assertThat(angel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Haste lets it attack the turn it enters the battlefield")
    void hasteLetsItAttackImmediately() {
        harness.setHand(player1, List.of(new LightningAngel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        GameService gameService = harness.getGameService();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gameService.declareAttackers(gameData, player1, List.of(0));

        Permanent angel = gameData.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(angel.isTapped()).isFalse();
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
