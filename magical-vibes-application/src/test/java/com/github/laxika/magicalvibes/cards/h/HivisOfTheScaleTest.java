package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CatacombDragon;
import com.github.laxika.magicalvibes.cards.i.IllicitAuction;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HivisOfTheScale.class, CatacombDragon.class, IronTuskElephant.class, IllicitAuction.class})
class HivisOfTheScaleTest extends BaseCardTest {

    @Test
    @DisplayName("{T} gains control of a target Dragon; Hivis stays tapped")
    void gainsControlOfDragon() {
        Permanent hivis = addReadyHivis(player1);
        Permanent dragon = addCreatureReady(player2, new CatacombDragon());

        activate(hivis, dragon);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(dragon.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(dragon.getId()));
        assertThat(hivis.isTapped()).isTrue();
        assertThat(gd.newestControlEffectFor(dragon.getId()).sourcePermanentId()).isEqualTo(hivis.getId());
    }

    @Test
    @DisplayName("Cannot target a non-Dragon creature")
    void cannotTargetNonDragon() {
        Permanent hivis = addReadyHivis(player1);
        Permanent elephant = addCreatureReady(player2, new IronTuskElephant());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hivis);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, elephant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Dragon");
    }

    @Test
    @DisplayName("Control is lost when Hivis untaps during its controller's untap step")
    void controlLostWhenHivisUntaps() {
        Permanent hivis = addReadyHivis(player1);
        Permanent dragon = addCreatureReady(player2, new CatacombDragon());

        activate(hivis, dragon);

        advanceToNextTurn(player1); // player2's turn — Hivis stays tapped, control retained
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(dragon.getId()));

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(hivis.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(dragon.getId()));
        assertThat(gd.controlEffectsFor(dragon.getId())).isEmpty();
    }

    @Test
    @DisplayName("Keeping Hivis tapped retains control across the controller's untap step")
    void keepingTappedRetainsControl() {
        Permanent hivis = addReadyHivis(player1);
        Permanent dragon = addCreatureReady(player2, new CatacombDragon());

        activate(hivis, dragon);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(hivis.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(dragon.getId()));
    }

    @Test
    @DisplayName("Control is lost when Hivis leaves the battlefield")
    void controlLostWhenHivisLeaves() {
        Permanent hivis = addReadyHivis(player1);
        Permanent dragon = addCreatureReady(player2, new CatacombDragon());

        activate(hivis, dragon);

        gd.playerBattlefields.get(player1.getId()).remove(hivis);
        advanceToNextTurn(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(dragon.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(dragon.getId()));
    }

    @Test
    @DisplayName("Control is lost when another player gains control of Hivis")
    void controlLostWhenHivisChangesController() {
        Permanent hivis = addReadyHivis(player1);
        Permanent dragon = addCreatureReady(player2, new CatacombDragon());

        activate(hivis, dragon);

        harness.setHand(player2, List.of(new IllicitAuction()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, hivis.getId());
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(hivis.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(dragon.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(dragon.getId()));
    }

    private void activate(Permanent hivis, Permanent target) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hivis);
        harness.activateAbility(player1, idx, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addReadyHivis(Player player) {
        Permanent perm = new Permanent(new HivisOfTheScale());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
