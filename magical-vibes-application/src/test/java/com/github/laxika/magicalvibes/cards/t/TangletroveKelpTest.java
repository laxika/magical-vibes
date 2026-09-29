package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangletroveKelp.class, GrizzlyBears.class})
class TangletroveKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Animates other Clues you control at the beginning of each combat")
    void animatesOtherCluesAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        Permanent ownClue = addClueToken(player1);
        Permanent opposingClue = addClueToken(player2);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.isCreature(gd, ownClue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownClue)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownClue)).isEqualTo(6);
        assertThat(gqs.hasEffectiveSubtype(gd, ownClue, CardSubtype.PLANT)).isTrue();
        assertThat(gqs.isCreature(gd, opposingClue)).isFalse();
    }

    @Test
    @DisplayName("Clue animation wears off at the end of the turn")
    void clueAnimationWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        Permanent clue = addClueToken(player1);

        advanceToCombatAndResolve(player1);
        assertThat(gqs.isCreature(gd, clue)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isCreature(gd, clue)).isFalse();
    }

    @Test
    @DisplayName("Sacrifices to draw a card")
    void sacrificesToDrawACard() {
        Permanent kelp = harness.addToBattlefieldAndReturn(player1, new TangletroveKelp());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int kelpIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kelp);
        harness.activateAbility(player1, kelpIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kelp);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
        return clue;
    }
}
