package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MtendaGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("During your upkeep, bounces itself and returns a Griffin card from your graveyard to hand")
    void bouncesSelfAndReturnsGriffin() {
        addReadyGriffin(player1);
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ekundu)));
        enterUpkeep();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(ekundu.getId()))
                .anyMatch(c -> c.getName().equals("Mtenda Griffin"));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(ekundu.getId()));
        harness.assertNotOnBattlefield(player1, "Mtenda Griffin");
    }

    @Test
    @DisplayName("Cannot target a non-Griffin card in your graveyard")
    void cannotTargetNonGriffin() {
        addReadyGriffin(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        enterUpkeep();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Mtenda Griffin");
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot target a Griffin in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        addReadyGriffin(player1);
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player2, new ArrayList<>(List.of(ekundu)));
        enterUpkeep();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        addReadyGriffin(player1);
        Card ekundu = new EkunduGriffin();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ekundu)));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(ekundu.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    private void enterUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 1);
    }

    private Permanent addReadyGriffin(Player player) {
        Permanent perm = new Permanent(new MtendaGriffin());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
