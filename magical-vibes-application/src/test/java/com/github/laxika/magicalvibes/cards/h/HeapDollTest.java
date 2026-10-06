package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeapDoll.class, GrizzlyBears.class, Cancel.class})
class HeapDollTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and exiles a card from controller's graveyard")
    void exilesFromOwnGraveyardAndSacrificesSelf() {
        Permanent doll = addCreatureReady(player1, new HeapDoll());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);
        harness.activateAbility(player1, dollIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doll);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile a card from an opponent's graveyard")
    void exilesFromOpponentGraveyard() {
        Permanent doll = addCreatureReady(player1, new HeapDoll());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);
        harness.activateAbility(player1, dollIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can exile any card type, not just creatures")
    void exilesNonCreatureCard() {
        Permanent doll = addCreatureReady(player1, new HeapDoll());
        Card cancel = new Cancel();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cancel)));

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);
        harness.activateAbility(player1, dollIndex, 0, null, cancel.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    @Test
    @DisplayName("Rejects a target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent doll = addCreatureReady(player1, new HeapDoll());
        Card bears = new GrizzlyBears();

        int dollIndex = gd.playerBattlefields.get(player1.getId()).indexOf(doll);

        assertThatThrownBy(() -> harness.activateAbility(player1, dollIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeTargetIsExiled() {
        Permanent doll = addCreatureReady(player1, new HeapDoll());
        Card target = new HeapDoll();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(doll);
        harness.assertInGraveyard(player1, "Heap Doll");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent doll = harness.addToBattlefieldAndReturn(player1, new HeapDoll());
        doll.setSummoningSick(true);
        doll.tap();
        Card target = new HeapDoll();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Heap Doll");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    void cannotTargetItselfBeforePayingSacrificeCost() {
        Permanent doll = addCreatureReady(player1, new HeapDoll());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                doll.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(doll);
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Heap Doll");
    }

    @Test
    void targetExiledInResponseDoesNotRefundSacrificeOrExileAnotherCard() {
        addCreatureReady(player1, new HeapDoll());
        addCreatureReady(player2, new HeapDoll());
        Card target = new HeapDoll();
        Card other = new HeapDoll();
        harness.setGraveyard(player2, List.of(target, other));

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player2, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(other);
        harness.assertInGraveyard(player1, "Heap Doll");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
