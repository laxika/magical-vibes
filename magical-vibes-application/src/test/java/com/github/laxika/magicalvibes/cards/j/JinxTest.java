package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JinxTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes the chosen basic land type until end of turn")
    void targetLandBecomesChosenType() {
        UUID forestId = castJinxOnOpponentForest();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The overridden land taps for the new type's mana")
    void overriddenLandTapsForNewColor() {
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefield(player1, new Forest());
        UUID forestId = harness.getPermanentId(player1, "Forest");
        harness.castInstant(player1, 0, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        int forestIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(gqs.findPermanentById(gd, forestId));
        gs.tapPermanent(gd, player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("The override wears off at end of turn")
    void overrideWearsOffAtEndOfTurn() {
        UUID forestId = castJinxOnOpponentForest();
        Permanent forest = gqs.findPermanentById(gd, forestId);

        forest.resetModifiers();

        assertThat(forest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Resolving schedules a draw that happens at the next upkeep")
    void schedulesDrawAtNextUpkeep() {
        castJinxOnOpponentForest();

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        StepTriggerService stepTriggerService = GameTestEngineContext.get().getBean(StepTriggerService.class);
        gd.activePlayerId = player2.getId();
        harness.inMutationScope(() -> stepTriggerService.handleUpkeepTriggers(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    /** Casts Jinx on the opponent's Forest and chooses Island; returns the Forest's id. */
    private UUID castJinxOnOpponentForest() {
        harness.setHand(player1, List.of(new Jinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.castInstant(player1, 0, forestId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        return forestId;
    }
}
