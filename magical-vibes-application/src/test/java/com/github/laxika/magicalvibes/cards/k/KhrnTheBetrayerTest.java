package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhrnTheBetrayer.class, GrizzlyBears.class, HillGiant.class, Shock.class})
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

        harness.castInstant(player2, 0, kharn.getId());
        harness.passBothPriorities();
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
}
