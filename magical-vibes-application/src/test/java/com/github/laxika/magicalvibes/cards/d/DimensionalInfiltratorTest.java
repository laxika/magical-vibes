package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DimensionalInfiltrator.class, Forest.class, GrizzlyBears.class})
class DimensionalInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's land and may return Dimensional Infiltrator to hand")
    void exilesLandAndMayReturnToHand() {
        Permanent infiltrator = addInfiltrator();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(infiltrator.getId()));
        harness.assertInHand(player1, "Dimensional Infiltrator");
    }

    @Test
    @DisplayName("Declining the land return keeps Dimensional Infiltrator on the battlefield")
    void decliningReturnKeepsInfiltratorOnBattlefield() {
        Permanent infiltrator = addInfiltrator();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(land));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(infiltrator.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("Exiling a nonland does not offer the return choice")
    void exilingNonlandDoesNotOfferReturnChoice() {
        Permanent infiltrator = addInfiltrator();
        Card nonland = new GrizzlyBears();
        harness.setLibrary(player2, List.of(nonland));
        addAbilityMana();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(infiltrator.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(nonland);
    }

    @Test
    @DisplayName("Can target only an opponent")
    void canTargetOnlyOpponent() {
        addInfiltrator();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private Permanent addInfiltrator() {
        return harness.addToBattlefieldAndReturn(player1, new DimensionalInfiltrator());
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
