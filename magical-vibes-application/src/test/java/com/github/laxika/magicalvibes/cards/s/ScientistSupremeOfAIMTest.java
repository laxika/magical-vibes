package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScientistSupremeOfAIM.class, RodOfRuin.class, ProdigalPyromancer.class})
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
        harness.passBothPriorities();
        harness.passBothPriorities();

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
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 1, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, pyromancer.getId()))
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
}
