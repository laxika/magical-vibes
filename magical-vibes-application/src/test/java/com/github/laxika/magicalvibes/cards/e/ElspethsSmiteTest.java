package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PhyrexianArchivist;
import com.github.laxika.magicalvibes.cards.w.WaryThespian;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElspethsSmite.class, PhyrexianArchivist.class, WaryThespian.class})
class ElspethsSmiteTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a small creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaryThespian());
        target.setAttacking(true);
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Deals 3 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArchivist());
        target.setAttacking(true);
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(target.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetCreatureOutsideCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WaryThespian());
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can damage and exile a blocking creature")
    void killsAndExilesBlockingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WaryThespian());
        target.setBlocking(true);
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Wary Thespian");
        harness.assertNotInGraveyard(player1, "Wary Thespian");
        assertThat(harness.getGameData().exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Does not damage or mark a creature that stops attacking before resolution")
    void targetLeavingCombatBeforeResolutionIsIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArchivist());
        target.setAttacking(true);
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Phyrexian Archivist");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.isExileInsteadOfDieThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Exiles a surviving creature when it dies later that turn after leaving combat")
    void exileReplacementPersistsAfterCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianArchivist());
        target.setAttacking(true);
        harness.setHand(player1, List.of(new ElspethsSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        target.setAttacking(false);
        target.setMarkedDamage(5);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Phyrexian Archivist");
        harness.assertNotInGraveyard(player2, "Phyrexian Archivist");
        assertThat(harness.getGameData().exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }
}
