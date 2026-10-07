package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WarbriarBlessing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToweringWaveMystic.class, Forest.class, GrizzlyBears.class, WarbriarBlessing.class})
class ToweringWaveMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes a chosen player mill that many cards")
    void combatDamageMillsChosenPlayer() {
        Permanent mystic = addCreatureReady(player1, new ToweringWaveMystic());
        mystic.setAttacking(true);
        setLibrary(player2, 5);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The target player can be the creature's controller")
    void canTargetController() {
        Permanent mystic = addCreatureReady(player1, new ToweringWaveMystic());
        mystic.setAttacking(true);
        setLibrary(player1, 5);
        setLibrary(player2, 5);

        resolveCombat();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Damage to a creature also triggers the mill ability")
    void damageToCreatureTriggers() {
        Permanent mystic = addCreatureReady(player1, new ToweringWaveMystic());
        mystic.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        setLibrary(player2, 5);

        resolveCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Towering-Wave Mystic");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(Forest.class::isInstance)
                .toList()).hasSize(2);
    }

    @Test
    @DisplayName("No damage produces no trigger")
    void noDamageProducesNoTrigger() {
        ToweringWaveMystic card = new ToweringWaveMystic();
        card.setPower(0);
        Permanent mystic = addCreatureReady(player1, card);
        mystic.setAttacking(true);
        setLibrary(player2, 5);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Fight damage triggers milling even though it is not combat damage")
    void fightDamageTriggersMill() {
        Permanent mystic = addCreatureReady(player1, new ToweringWaveMystic());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        setLibrary(player2, 5);
        harness.setHand(player1, List.of(new WarbriarBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, mystic.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(mystic.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(Forest.class::isInstance).toList()).hasSize(2);
    }

    @Test
    @DisplayName("Milling more cards than remain mills the entire library")
    void millsOnlyRemainingCards() {
        Permanent mystic = addCreatureReady(player1, new ToweringWaveMystic());
        mystic.setAttacking(true);
        setLibrary(player2, 1);

        resolveCombat();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void setLibrary(Player player, int size) {
        harness.setLibrary(player, List.<Card>of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest())
                .subList(0, size));
    }
}
