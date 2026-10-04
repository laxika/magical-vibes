package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KheruMindEater.class, GrizzlyBears.class})
class KheruMindEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player exile a chosen hand card face down")
    void combatDamageExilesChosenHandCardFaceDown() {
        Permanent mindEater = addAttackingMindEater();
        Card bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);

        ExiledCardEntry exiled = gd.findExiledCard(bears.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(mindEater.getId());
        assertThat(exiled.faceDown()).isTrue();
    }

    @Test
    @DisplayName("The controller may cast a card exiled face down with Kheru Mind-Eater")
    void controllerMayCastExiledCard() {
        addAttackingMindEater();
        Card bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Cards exiled with Kheru Mind-Eater cannot be cast after it leaves the battlefield")
    void permissionEndsWhenSourceLeaves() {
        Permanent mindEater = addAttackingMindEater();
        Card bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));

        resolveCombatAndTrigger();
        harness.handleCardChosen(player2, 0);
        gd.playerBattlefields.get(player1.getId()).remove(mindEater);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingMindEater() {
        Permanent mindEater = addCreatureReady(player1, new KheruMindEater());
        mindEater.setAttacking(true);
        return mindEater;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
