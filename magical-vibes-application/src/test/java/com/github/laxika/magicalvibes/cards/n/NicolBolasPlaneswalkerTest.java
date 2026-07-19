package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NicolBolasPlaneswalkerTest extends BaseCardTest {

    // ===== +3: Destroy target noncreature permanent =====

    @Test
    @DisplayName("+3 destroys a target noncreature permanent")
    void plusThreeDestroysNoncreaturePermanent() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, 0, null, forestId);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(8); // 5 + 3
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(forestId));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("+3 cannot target a creature")
    void plusThreeCannotTargetCreature() {
        addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== -2: Gain control of target creature =====

    @Test
    @DisplayName("-2 gains permanent control of a target creature")
    void minusTwoGainsControlOfCreature() {
        Permanent bolas = addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 1, null, bearId);
        harness.passBothPriorities();

        assertThat(bolas.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 5 - 2
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(bearId));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(bearId));
        assertThat(gd.newestControlEffectFor(bearId).duration()).isEqualTo(EffectDuration.PERMANENT);
    }

    @Test
    @DisplayName("-2 cannot target a noncreature permanent")
    void minusTwoCannotTargetNoncreature() {
        addReadyBolas(player1, 5);
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== -9: 7 damage + discard 7 + sacrifice 7 (player or planeswalker) =====

    @Test
    @DisplayName("-9 deals 7 damage to the targeted player and makes them sacrifice permanents")
    void minusNineDamagesAndSacrificesTargetPlayer() {
        addReadyBolas(player1, 9);
        harness.setHand(player2, new ArrayList<>());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        int p2LifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 7);
        // Fewer permanents than the seven required, so all are sacrificed with no choice.
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-9 makes the targeted player discard seven cards")
    void minusNineDiscardsFromTargetPlayer() {
        addReadyBolas(player1, 9);
        List<com.github.laxika.magicalvibes.model.Card> hand = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            hand.add(new GrizzlyBears());
        }
        harness.setHand(player2, hand);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Discard seven of the eight cards; the targeted player chooses which.
        for (int i = 0; i < 7; i++) {
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-9 targeting a planeswalker routes the sacrifice to that planeswalker's controller")
    void minusNineSacrificeRoutesToPlaneswalkerController() {
        addReadyBolas(player1, 9);

        // High-loyalty planeswalker survives the 7 damage so its controller is resolvable when the
        // sacrifice rider runs; it is also a permanent its controller may then be forced to sacrifice.
        AjaniOutlandChaperone ajaniCard = new AjaniOutlandChaperone();
        Permanent ajani = new Permanent(ajaniCard);
        ajani.setCounterCount(CounterType.LOYALTY, 10);
        gd.playerBattlefields.get(player2.getId()).add(ajani);

        harness.setHand(player2, new ArrayList<>());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, ajani.getId());
        harness.passBothPriorities();

        // Fewer than seven permanents, so the planeswalker's controller (player2) sacrifices all of
        // them with no choice — proving the rider routed to player2, not the caster.
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot use -9 when loyalty is insufficient")
    void cannotActivateMinusNineWithInsufficientLoyalty() {
        addReadyBolas(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    // ===== Helpers =====

    private Permanent addReadyBolas(Player player, int loyalty) {
        NicolBolasPlaneswalker card = new NicolBolasPlaneswalker();
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
