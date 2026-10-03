package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NomadsEnKor;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Disintegrate.class, GrizzlyBears.class, DrudgeSkeletons.class, Terror.class, NomadsEnKor.class})
class DisintegrateTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to target player")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Creature killed by Disintegrate is exiled instead of going to graveyard")
    void creatureKilledIsExiledInsteadOfDying() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature that survives damage is not exiled")
    void creatureThatSurvivesIsNotExiled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature that survives damage is exiled if it dies later that turn")
    void creatureThatSurvivesDamageIsExiledIfItDiesLaterThatTurn() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 1, targetId);

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature remains marked for exile if Disintegrate damage is prevented")
    void creatureWithPreventedDisintegrateDamageIsExiledIfItDiesLaterThatTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setDamagePreventionShield(1);

        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature cannot regenerate even when Disintegrate damage is prevented")
    void creatureCannotRegenerateWhenDamageIsPrevented() {
        Permanent skeleton = addCreatureReady(player2, new DrudgeSkeletons());
        skeleton.setRegenerationShield(1);
        skeleton.setDamagePreventionShield(1);

        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 1, skeleton.getId());

        assertThat(skeleton.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(skeleton);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        skeleton.setBlocking(true);
        skeleton.addBlockingTargetId(attacker.getId());
        resolveCombat(player1);

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
    }
    @Test
    @DisplayName("X zero still exiles the targeted creature if it dies that turn")
    void zeroDamageStillAppliesExileReplacement() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exile replacement expires at the end of the turn")
    void exileReplacementExpiresAtEndOfTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 1, bears.getId());

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
    @Test
    @DisplayName("Redirected damage does not apply the exile rider to its recipient")
    void redirectedDamageDoesNotExileRecipient() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1, nomads.getId());

        assertThat(nomads.getMarkedDamage()).isZero();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature receiving redirected damage can still regenerate")
    void redirectedDamageRecipientCanRegenerate() {
        Permanent nomads = addCreatureReady(player1, new NomadsEnKor());
        Permanent skeleton = addCreatureReady(player1, new DrudgeSkeletons());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, skeleton.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Disintegrate()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 1, nomads.getId());

        harness.assertOnBattlefield(player1, "Drudge Skeletons");
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(skeleton.getMarkedDamage()).isZero();
        assertThat(skeleton.getRegenerationShield()).isZero();
        assertThat(nomads.getMarkedDamage()).isZero();
    }
}
