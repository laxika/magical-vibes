package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaxisTheTroublemaker.class, GrizzlyBears.class})
class JaxisTheTroublemakerTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability creates a hasty token copy and discards a card")
    void activatedAbilityCreatesHastyTokenCopy() {
        addJaxisReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findToken(player1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The activated token is sacrificed at the next end step and draws a card")
    void activatedTokenDiesAtNextEndStepAndDraws() {
        addJaxisReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent token = findToken(player1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The activated ability cannot target Jaxis itself or an opposing creature")
    void activatedAbilityRequiresAnotherCreatureYouControl() {
        Permanent jaxis = addJaxisReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jaxis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new JaxisTheTroublemaker()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jaxis = findPermanent(player1, "Jaxis, the Troublemaker");
        assertThat(gqs.hasKeyword(gd, jaxis, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Jaxis, the Troublemaker");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addJaxisReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JaxisTheTroublemaker());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
