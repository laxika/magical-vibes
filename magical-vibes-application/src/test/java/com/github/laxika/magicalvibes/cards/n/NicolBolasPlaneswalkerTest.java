package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AjaniOutlandChaperone;
import com.github.laxika.magicalvibes.cards.c.CanyonMinotaur;
import com.github.laxika.magicalvibes.cards.e.ExtractorDemon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NicolBolasPlaneswalker.class, AjaniOutlandChaperone.class, Forest.class,
        GrizzlyBears.class, CanyonMinotaur.class, ExtractorDemon.class})
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
        harness.assertInGraveyard(player2, "Forest");
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
        harness.assertNotOnBattlefield(player2, "Forest");
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
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniOutlandChaperone());
        ajani.setCounterCount(CounterType.LOYALTY, 10);

        harness.setHand(player2, new ArrayList<>());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, 2, null, ajani.getId());
        harness.passBothPriorities();

        // Fewer than seven permanents, so the planeswalker's controller (player2) sacrifices all of
        // them with no choice — proving the rider routed to player2, not the caster.
        harness.assertNotOnBattlefield(player2, "Forest");
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

    @Test
    @DisplayName("-2 still steals the creature when paying the cost removes Nicol Bolas")
    void minusTwoResolvesAfterSourceDiesFromLoyaltyCost() {
        addReadyBolas(player1, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CanyonMinotaur());

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.assertNotOnBattlefield(player1, "Nicol Bolas, Planeswalker");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canyon Minotaur");
        harness.assertNotOnBattlefield(player2, "Canyon Minotaur");
        harness.assertInGraveyard(player1, "Nicol Bolas, Planeswalker");
    }

    @Test
    @DisplayName("-9 cannot target a creature")
    void minusNineCannotTargetCreature() {
        addReadyBolas(player1, 9);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CanyonMinotaur());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-9 finishes discarding before the target chooses seven permanents to sacrifice")
    void minusNineDiscardsThenSacrificesChosenSeven() {
        addReadyBolas(player1, 9);
        List<com.github.laxika.magicalvibes.model.Card> hand = new ArrayList<>();
        List<UUID> forestIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            hand.add(new CanyonMinotaur());
            forestIds.add(harness.addToBattlefieldAndReturn(player2, new Forest()).getId());
        }
        harness.setHand(player2, hand);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(8);
        for (int i = 0; i < 7; i++) {
            harness.handleCardChosen(player2, 0);
            assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(8);
        }
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.handleMultiplePermanentsChosen(player2, forestIds.subList(0, 7));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).containsExactly(forestIds.get(7));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Canyon Minotaur")).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Forest")).hasSize(7);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-9 still makes a zero-loyalty planeswalker's controller discard and sacrifice")
    void minusNineRoutesRidersAfterPlaneswalkerLosesAllLoyalty() {
        addReadyBolas(player1, 9);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        target.setCounterCount(CounterType.LOYALTY, 7);
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new CanyonMinotaur()));
        harness.setHand(player1, List.of(new CanyonMinotaur()));

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Canyon Minotaur");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Nicol Bolas, Planeswalker");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Canyon Minotaur");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("-9 sacrifices seven permanents simultaneously so Extractor Demon sees another creature leave")
    void minusNineSacrificePreservesSimultaneousLeavesTriggers() {
        addReadyBolas(player1, 9);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new ExtractorDemon());
        harness.addToBattlefield(player2, new CanyonMinotaur());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.setLibrary(player1, List.of(new CanyonMinotaur(), new CanyonMinotaur(),
                new CanyonMinotaur()));

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Canyon Minotaur")).hasSize(2);
    }

    private Permanent addReadyBolas(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new NicolBolasPlaneswalker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
