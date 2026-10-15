package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EssenceFlux;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RagefireHellkite.class, EssenceFlux.class})
class RagefireHellkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gives Ragefire Hellkite double strike")
    void sacrificingAnotherCreatureGrantsDoubleStrike() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new RagefireHellkite());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not give Ragefire Hellkite double strike")
    void decliningSacrificeDoesNothing() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new RagefireHellkite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new RagefireHellkite());

        attackAndAcceptMay();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void attackAndAcceptMay() {
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsCreature() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent bears = addCreatureReady(player1, new RagefireHellkite());
        Permanent opposingHellkite = addCreatureReady(player2, new RagefireHellkite());

        attackAndAcceptMay();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, hellkite.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingHellkite.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingHellkite);
        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void noOtherCreatureMeansNoDoubleStrike() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        addCreatureReady(player2, new RagefireHellkite());

        attackAndAcceptMay();

        assertThat(gqs.hasKeyword(gd, hellkite, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hellkite);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(EssenceFlux.class)
    void returnedHellkiteDoesNotGainDoubleStrikeFromItsOldAttackTrigger() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        Permanent sacrifice = addCreatureReady(player1, new RagefireHellkite());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.castInstant(player1, 0, hellkite.getId());
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == hellkite.getCard()).findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(hellkite.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gqs.hasKeyword(gd, returned, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @CardUsed(EssenceFlux.class)
    void returnedHellkiteCanBeSacrificedToItsOldAttackTrigger() {
        Permanent hellkite = addCreatureReady(player1, new RagefireHellkite());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.castInstant(player1, 0, hellkite.getId());
        harness.passBothPriorities();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == hellkite.getCard()).findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(hellkite.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, returned.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned.getCard());
    }
}
