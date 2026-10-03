package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.MarshmistTitan;
import com.github.laxika.magicalvibes.cards.n.NyxbornShieldmate;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({BoltOfKeranos.class, NyxbornShieldmate.class, MarshmistTitan.class})
class BoltOfKeranosTest extends BaseCardTest {

    private void castBolt(UUID targetId) {
        harness.setHand(player1, List.of(new BoltOfKeranos()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals 3 damage to target player, then scries 1")
    void damagesPlayerAndScries() {
        harness.setLife(player2, 20);

        castBolt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Deals 3 damage to a creature, killing a 1/2")
    void killsSmallCreature() {
        harness.addToBattlefield(player2, new NyxbornShieldmate());

        castBolt(harness.getPermanentId(player2, "Nyxborn Shieldmate"));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Nyxborn Shieldmate");
        harness.assertInGraveyard(player2, "Nyxborn Shieldmate");
    }

    @Test
    @DisplayName("A 4/5 survives with three damage marked")
    void largerCreatureSurvives() {
        harness.addToBattlefield(player2, new MarshmistTitan());

        castBolt(harness.getPermanentId(player2, "Marshmist Titan"));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertOnBattlefield(player2, "Marshmist Titan");
        assertThat(findPermanent(player2, "Marshmist Titan").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Completing the scry finishes resolution and the spell goes to the graveyard")
    void scryCompletesAndSpellGoesToGraveyard() {
        castBolt(player2.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bolt of Keranos");
    }

    @Test
    @DisplayName("Fizzles if the only target is removed — no damage, no scry")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new NyxbornShieldmate());
        UUID targetId = harness.getPermanentId(player2, "Nyxborn Shieldmate");
        harness.setHand(player1, List.of(new BoltOfKeranos()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        harness.assertInGraveyard(player1, "Bolt of Keranos");
    }

    @Test
    @DisplayName("Scry can keep the top card and does not change the opponent's library")
    void scryKeepsTopCard() {
        NyxbornShieldmate top = new NyxbornShieldmate();
        MarshmistTitan bottom = new MarshmistTitan();
        BoltOfKeranos opponentCard = new BoltOfKeranos();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setLibrary(player2, List.of(opponentCard));

        castBolt(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, bottom);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        harness.assertInGraveyard(player1, "Bolt of Keranos");
    }

    @Test
    @DisplayName("Scry can put the top card on the bottom after damaging its controller")
    void scryPutsCardOnBottomAfterTargetingSelf() {
        NyxbornShieldmate top = new NyxbornShieldmate();
        MarshmistTitan bottom = new MarshmistTitan();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setLife(player1, 20);

        castBolt(player1.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertLife(player1, 17);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        harness.assertInGraveyard(player1, "Bolt of Keranos");
    }

    @Test
    @DisplayName("An empty library does not stop damage or spell resolution")
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setLife(player2, 20);

        castBolt(player2.getId());

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Bolt of Keranos");
    }
}
