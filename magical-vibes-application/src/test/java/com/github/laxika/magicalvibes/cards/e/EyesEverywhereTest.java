package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EyesEverywhereTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 at the beginning of its controller's upkeep")
    void scriesAtBeginningOfUpkeep() {
        Card top = new GrizzlyBears();
        Card bottom = new LlanowarElves();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.addToBattlefield(player1, new EyesEverywhere());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
    }

    @Test
    @DisplayName("Exchanges control of itself and the target nonland permanent")
    void exchangesControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();
        prepareForSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId).contains(source.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId).contains(target.getId());
    }

    @Test
    @DisplayName("Cannot activate the exchange at instant speed")
    void exchangeRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new EyesEverywhere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        addActivationMana();
        prepareForSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("required predicate");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void prepareForSorcerySpeedActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
