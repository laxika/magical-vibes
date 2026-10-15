package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RiveteersInitiate;
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

@CardUsed({JaxisTheTroublemaker.class, RiveteersInitiate.class})
class JaxisTheTroublemakerTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability creates a hasty token copy and discards a card")
    void activatedAbilityCreatesHastyTokenCopy() {
        addCreatureReady(player1, new JaxisTheTroublemaker());
        harness.addToBattlefield(player1, new RiveteersInitiate());
        harness.setHand(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Riveteers Initiate");
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
        addCreatureReady(player1, new JaxisTheTroublemaker());
        harness.addToBattlefield(player1, new RiveteersInitiate());
        harness.setHand(player1, List.of(new RiveteersInitiate()));
        harness.setLibrary(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Riveteers Initiate");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent token = findToken(player1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(findPermanents(player1, "Riveteers Initiate")).hasSize(1);
        harness.assertInHand(player1, "Riveteers Initiate");
    }

    @Test
    @DisplayName("The activated ability cannot target Jaxis itself or an opposing creature")
    void activatedAbilityRequiresAnotherCreatureYouControl() {
        Permanent jaxis = addCreatureReady(player1, new JaxisTheTroublemaker());
        harness.setHand(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, jaxis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");

        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RiveteersInitiate());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature you control");
    }

    @Test
    @DisplayName("Blitz grants haste, draws on death, and sacrifices at the next end step")
    void blitzGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new JaxisTheTroublemaker()));
        harness.setLibrary(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent jaxis = findPermanent(player1, "Jaxis, the Troublemaker");
        assertThat(gqs.hasKeyword(gd, jaxis, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Jaxis, the Troublemaker");
        harness.assertInHand(player1, "Riveteers Initiate");
    }

    @Test
    @DisplayName("Blitz haste is present immediately when Jaxis resolves")
    void blitzHasteDoesNotWaitForAnEnterTrigger() {
        harness.setHand(player1, List.of(new JaxisTheTroublemaker()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent jaxis = findPermanent(player1, "Jaxis, the Troublemaker");
        assertThat(gqs.hasKeyword(gd, jaxis, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Normal casting does not grant blitz benefits or schedule sacrifice")
    void normalCastDoesNotBlitz() {
        harness.setHand(player1, List.of(new JaxisTheTroublemaker()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent jaxis = findPermanent(player1, "Jaxis, the Troublemaker");
        assertThat(gqs.hasKeyword(gd, jaxis, Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Jaxis, the Troublemaker");
    }

    @Test
    @DisplayName("Copying a Jaxis token does not copy its granted death ability")
    void copyingTokenGrantsOnlyOneDeathDrawToEachToken() {
        Permanent jaxis = addCreatureReady(player1, new JaxisTheTroublemaker());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new RiveteersInitiate());
        harness.setHand(player1, List.of(new RiveteersInitiate(), new RiveteersInitiate()));
        harness.setLibrary(player1, List.of(new RiveteersInitiate(), new RiveteersInitiate(),
                new RiveteersInitiate(), new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, original.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent firstToken = findToken(player1);

        jaxis.untap();
        harness.activateAbility(player1, 0, null, firstToken.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Riveteers Initiate")).containsExactly(original);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Jaxis cannot activate its copy ability during combat")
    void copyAbilityRequiresSorceryTiming() {
        addCreatureReady(player1, new JaxisTheTroublemaker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new RiveteersInitiate());
        harness.setHand(player1, List.of(new RiveteersInitiate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent findToken(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
