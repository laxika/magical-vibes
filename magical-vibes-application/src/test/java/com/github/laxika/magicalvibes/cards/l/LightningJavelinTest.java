package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraFireOfKaladesh;
import com.github.laxika.magicalvibes.cards.c.ChandraRoaringFlame;
import com.github.laxika.magicalvibes.cards.p.Prickleboar;
import com.github.laxika.magicalvibes.cards.r.RhoxMaulers;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningJavelin.class, TimberpackWolf.class, RhoxMaulers.class, Prickleboar.class,
        ChandraFireOfKaladesh.class, ChandraRoaringFlame.class})
class LightningJavelinTest extends BaseCardTest {

    private void castJavelin(UUID targetId) {
        harness.setHand(player1, List.of(new LightningJavelin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("Deals 3 damage to target player, then scries 1")
    void damagesPlayerAndScries() {
        harness.setLife(player2, 20);

        castJavelin(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Deals 3 damage to a creature, killing a 2/2")
    void killsSmallCreature() {
        harness.addToBattlefield(player2, new TimberpackWolf());

        castJavelin(harness.getPermanentId(player2, "Timberpack Wolf"));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Timberpack Wolf");
        harness.assertInGraveyard(player2, "Timberpack Wolf");
    }

    @Test
    @DisplayName("A 4/4 survives with 3 damage marked")
    void largerCreatureSurvives() {
        harness.addToBattlefield(player2, new RhoxMaulers());

        castJavelin(harness.getPermanentId(player2, "Rhox Maulers"));

        harness.assertOnBattlefield(player2, "Rhox Maulers");
        assertThat(findPermanent(player2, "Rhox Maulers").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature with exactly 3 toughness dies after the spell finishes resolving")
    void killsCreatureWithThreeToughness() {
        harness.addToBattlefield(player2, new Prickleboar());

        castJavelin(harness.getPermanentId(player2, "Prickleboar"));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Prickleboar");
        harness.assertInGraveyard(player2, "Prickleboar");
        harness.assertInGraveyard(player1, "Lightning Javelin");
    }

    @Test
    @DisplayName("Completing the scry finishes resolution and the spell goes to the graveyard")
    void scryCompletesAndSpellGoesToGraveyard() {
        castJavelin(player2.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Javelin");
    }

    @Test
    @DisplayName("Scry can keep the top card without changing the remaining library")
    void keepsTopCard() {
        var top = new TimberpackWolf();
        var second = new RhoxMaulers();
        harness.setLibrary(player1, List.of(top, second));

        castJavelin(player2.getId());

        var scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player1.getId());
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        harness.assertInGraveyard(player1, "Lightning Javelin");
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom")
    void bottomsTopCard() {
        var top = new TimberpackWolf();
        var second = new RhoxMaulers();
        harness.setLibrary(player1, List.of(top, second));

        castJavelin(player2.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Javelin");
    }

    @Test
    @DisplayName("An empty library does not prevent damage or completion of the spell")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);

        castJavelin(player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Javelin");
    }

    @Test
    @DisplayName("Can target its controller and still scry")
    void damagesController() {
        harness.setLife(player1, 20);

        castJavelin(player1.getId());

        harness.assertLife(player1, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Deals 3 damage to a planeswalker, removing loyalty, then scries")
    void damagesPlaneswalker() {
        var front = new ChandraFireOfKaladesh();
        var chandra = harness.addToBattlefieldAndReturn(player2, front);
        chandra.setCard(front.getBackFaceCard());
        chandra.setTransformed(true);
        chandra.setCounterCount(CounterType.LOYALTY, 4);

        castJavelin(chandra.getId());

        harness.assertOnBattlefield(player2, "Chandra, Roaring Flame");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Fizzles if the only target is removed — no damage, no scry")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new TimberpackWolf());
        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");
        harness.setHand(player1, List.of(new LightningJavelin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Lightning Javelin");
    }
}
