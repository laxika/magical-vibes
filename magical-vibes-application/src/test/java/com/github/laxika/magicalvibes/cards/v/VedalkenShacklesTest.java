package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.cards.f.FangrenPathcutter;
import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({VedalkenShackles.class, Arachnoid.class, FangrenPathcutter.class, Island.class, AvariceTotem.class})
class VedalkenShacklesTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature whose power is at most the number of Islands you control")
    void gainsControlWithinIslandCount() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(shackles.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature whose power exceeds the number of Islands you control")
    void rejectsCreatureAboveIslandCount() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new FangrenPathcutter());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, shackles), null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("number of Islands");
    }

    @Test
    @DisplayName("Does not gain control if Vedalken Shackles untaps before the ability resolves")
    void doesNotGainControlIfSourceUntapsBeforeResolution() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        shackles.untap();
        harness.passBothPriorities();

        assertThat(shackles.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Rechecks the Island count when the ability resolves")
    void islandCountIsRecheckedAtResolution() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Island);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Losing Islands after resolution does not end control")
    void islandCountIsNotRecheckedAfterResolution() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof Island);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Untapping Vedalken Shackles ends control")
    void untappingEndsControl() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(shackles.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Control continues if another player gains control of the tapped Shackles")
    void sourceControllerChangeDoesNotEndControl() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new AvariceTotem()));
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        Permanent totem = findPermanent(player2, "Avarice Totem");
        harness.activateAbility(player2, battlefieldIndex(player2, totem), null, shackles.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shackles);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Choosing not to untap Vedalken Shackles keeps control")
    void choosingNotToUntapKeepsControl() {
        addIslands(player1, 2);
        Permanent shackles = addReadyShackles(player1);
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, shackles), null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(shackles.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    private Permanent addReadyShackles(Player player) {
        Permanent shackles = harness.addToBattlefieldAndReturn(player, new VedalkenShackles());
        shackles.setSummoningSick(false);
        return shackles;
    }

    private void addIslands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Island());
        }
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
