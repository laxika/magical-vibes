package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EchoChamberTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent picks one of their creatures and the controller gets a hasty token copy")
    void opponentChoosesCreatureToCopy() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.OpponentChoosesCreatureTheyControlToCopy.class);

        harness.handlePermanentChosen(player2, spider.getId());

        Permanent token = tokenCopy();
        assertThat(token.getCard().getName()).isEqualTo("Giant Spider");
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        // The token is the controller's, not the choosing opponent's.
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    @DisplayName("The choice auto-resolves when the opponent controls exactly one creature")
    void singleCreatureIsCopiedWithoutPrompt() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(tokenCopy().getCard().getName()).isEqualTo("Giant Spider");
    }

    @Test
    @DisplayName("Nothing happens when the opponent controls no creature")
    void noCreatureToCopy() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The token is exiled at the beginning of the next end step")
    void tokenExiledAtEndStep() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GiantSpider());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(tokenCopy()).isNotNull();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Cannot be activated at instant speed")
    void cannotActivateAtInstantSpeed() {
        setupEchoChamberOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player2, new GiantSpider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent tokenCopy() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No token copy was created"));
    }

    private void setupEchoChamberOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new EchoChamber());
        findPermanent(player1, "Echo Chamber").setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
