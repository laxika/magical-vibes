package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhrnTheBetrayer.class, GrizzlyBears.class, HillGiant.class, Shock.class,
        Skullcrack.class, TurnToFrog.class})
class KhrnTheBetrayerTest extends BaseCardTest {

    @Test
    @DisplayName("Khârn must attack and block each combat when able")
    void mustAttackAndBlockEachCombat() {
        Permanent kharn = addCreatureReady(player1, new KhrnTheBetrayer());

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .hasMessageContaining("must attack");

        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        prepareDeclareBlockers(player2);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of()))
                .hasMessageContaining("must block");
        assertThat(kharn).isNotNull();
    }

    @Test
    @DisplayName("Damage to Khârn is prevented, transfers it, and its former controller draws")
    void damageTransfersKharnAndFormerControllerDraws() {
        Permanent kharn = harness.addToBattlefieldAndReturn(player1, new KhrnTheBetrayer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, kharn.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kharn);
        assertThat(kharn.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Khârn's Sigil of Corruption triggers when it leaves the battlefield")
    void leavesBattlefieldAndFormerControllerDraws() {
        Permanent kharn = harness.addToBattlefieldAndReturn(player1, new KhrnTheBetrayer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, kharn));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void unpreventableDamageStillTransfersControlBeforeKharnDies() {
        Permanent kharn = harness.addToBattlefieldAndReturn(player1, new KhrnTheBetrayer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new Skullcrack(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, kharn.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kharn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(kharn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kharn.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void losingAbilitiesDisablesDamagePreventionAndControlTransfer() {
        Permanent kharn = harness.addToBattlefieldAndReturn(player1, new KhrnTheBetrayer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new TurnToFrog(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, kharn.getId());
        harness.castAndResolveInstant(player2, 0, kharn.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kharn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(kharn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kharn.getCard());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void combatDamageTransfersKharnButHeStillDealsSimultaneousDamage() {
        Permanent kharn = addCreatureReady(player1, new KhrnTheBetrayer());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        kharn.setAttacking(true);
        kharn.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTargetId(kharn.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kharn).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kharn);
        assertThat(kharn.getMarkedDamage()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void successiveDamageEventsTransferKharnBackAndEachFormerControllerDraws() {
        Permanent kharn = harness.addToBattlefieldAndReturn(player1, new KhrnTheBetrayer());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, kharn.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kharn);

        harness.castAndResolveInstant(player1, 0, kharn.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kharn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(kharn);
        assertThat(kharn.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
