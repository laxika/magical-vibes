package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DemonicCounsel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YawgmothDemon;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinVoid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SearchElemental.class, DemonicCounsel.class, GrizzlyBears.class, YawgmothDemon.class,
        ZhalfirinVoid.class})
class SearchElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Searching your library triggers scry 1")
    void searchingYourLibraryTriggersScry() {
        harness.addToBattlefield(player1, new SearchElemental());
        Card searchedCard = new YawgmothDemon();
        harness.setLibrary(player1, List.of(searchedCard, new GrizzlyBears()));
        harness.setHand(player1, List.of(new DemonicCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .hasSize(1);
    }

    @Test
    @DisplayName("Scrying puts a counter on Search Elemental and makes it unblockable this turn")
    void scryingPutsCounterAndMakesUnblockable() {
        Permanent elemental = readyPermanent(player1, new SearchElemental());
        Permanent blocker = readyPermanent(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.enterBattlefieldAndReturn(player1, new ZhalfirinVoid());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        elemental.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(elemental)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    private Permanent readyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
