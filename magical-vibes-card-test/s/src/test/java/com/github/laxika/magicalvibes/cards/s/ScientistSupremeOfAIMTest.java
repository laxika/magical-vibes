package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScientistSupremeOfAIM.class, RodOfRuin.class, ProdigalPyromancer.class, HowlingMine.class})
class ScientistSupremeOfAIMTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an activated ability from an artifact source")
    void copiesArtifactActivatedAbility() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID rodAbilityId = gd.stack.getLast().getCard().getId();
        harness.activateAbility(player1, 0, null, rodAbilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can be activated only once each turn")
    void onlyOnceEachTurn() {
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID rodAbilityId = gd.stack.getLast().getCard().getId();
        harness.activateAbility(player1, 0, null, rodAbilityId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rodAbilityId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Cannot target an ability from a nonartifact source")
    void cannotTargetNonartifactAbility() {
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gd.stack.getLast().getTargetableId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be activated only during its controller's turn")
    void onlyDuringItsControllersTurn() {
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    void canChooseNewTargetWithoutChangingOriginal() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        harness.addToBattlefield(player1, new RodOfRuin());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, player2.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, abilityId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotCopyOpponentsArtifactAbility() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        harness.addToBattlefield(player2, new RodOfRuin());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.activateAbility(player2, 0, null, player1.getId());
        UUID abilityId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, abilityId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void copiesNontargetedArtifactTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ScientistSupremeOfAIM());
        harness.addToBattlefield(player1, new HowlingMine());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(player1, TurnStep.DRAW);

        UUID triggerId = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, triggerId);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }
}
