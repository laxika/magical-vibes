package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceMemoryAdept;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

class ChandraPyromasterTest extends BaseCardTest {

    @Test
    @DisplayName("+1 pings the target player and a creature they control, which then can't block")
    void plusOneHitsPlayerAndTheirCreature() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(bear.isCantBlockThisTurn()).isTrue();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may choose the player alone, leaving every creature able to block")
    void plusOneCreatureTargetIsOptional() {
        addReadyChandra(player1, 4);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(bear.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("+1 can target a planeswalker and a creature its controller controls")
    void plusOneHitsPlaneswalkerAndItsControllersCreature() {
        addReadyChandra(player1, 4);
        Permanent jace = new Permanent(new JaceMemoryAdept());
        jace.setCounterCount(CounterType.LOYALTY, 4);
        gd.playerBattlefields.get(player2.getId()).add(jace);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(jace.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 rejects a creature the targeted player does not control")
    void plusOneRejectsCreatureOfAnotherController() {
        addReadyChandra(player1, 4);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("0 exiles the top card and grants permission to play it this turn")
    void zeroExilesTopCardWithPlayPermission() {
        Permanent chandra = addReadyChandra(player1, 4);
        Card top = new Shock();
        harness.setLibrary(player1, List.of(top, new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.exilePlayPermissions.get(top.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
        // Chandra's 0 grants a normal-cost play, not a free one.
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(top.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-7 exiles ten cards and asks which exiled instant or sorcery to copy")
    void ultimateExilesTenAndPromptsForCopyChoice() {
        addReadyChandra(player1, 7);
        Shock shock = new Shock();
        List<Card> library = new ArrayList<>();
        library.add(shock);
        for (int i = 0; i < 11; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExiledSpellCopyChoice.class);
    }

    @Test
    @DisplayName("-7 casts three free copies of the chosen instant or sorcery")
    void ultimateCastsThreeCopies() {
        addReadyChandra(player1, 7);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Shock shock = new Shock();
        List<Card> library = new ArrayList<>();
        library.add(shock);
        for (int i = 0; i < 11; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        // Each copy pauses for its own target; aim all three at the opponent.
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Shock")).count())
                .isEqualTo(3);
        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Shock")))
                .allMatch(e -> e.isCopy());
    }

    @Test
    @DisplayName("-7 does nothing further when no instant or sorcery is exiled")
    void ultimateWithoutInstantOrSorcery() {
        addReadyChandra(player1, 7);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            library.add(new Forest());
        }
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(10);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent perm = new Permanent(new ChandraPyromaster());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
