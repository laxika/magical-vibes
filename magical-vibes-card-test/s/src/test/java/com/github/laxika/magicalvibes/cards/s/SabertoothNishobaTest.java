package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarVanguard;
import com.github.laxika.magicalvibes.cards.m.ManiacalRage;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.cards.v.ViashinoGrappler;
import com.github.laxika.magicalvibes.cards.v.VodalianMerchant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SabertoothNishoba.class, LlanowarVanguard.class, ViashinoGrappler.class, VodalianMerchant.class, ScorchingLava.class, Repulse.class, ManiacalRage.class})
class SabertoothNishobaTest extends BaseCardTest {

    @Test
    void hasProtectionFromBlueAndRedButNotGreen() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player1, new SabertoothNishoba());

        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, nishoba, CardColor.BLACK)).isFalse();
    }

    @Test
    void blueCreatureCannotBlockSabertoothNishoba() {
        Permanent nishoba = addCreatureReady(player1, new SabertoothNishoba());
        nishoba.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new VodalianMerchant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void redCreatureCannotBlockSabertoothNishoba() {
        Permanent nishoba = addCreatureReady(player1, new SabertoothNishoba());
        nishoba.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ViashinoGrappler());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void trampleDealsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SabertoothNishoba());
        Permanent blocker = addCreatureReady(player2, new LlanowarVanguard());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void protectionPreventsRedCombatDamageWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new ViashinoGrappler());
        Permanent nishoba = addCreatureReady(player2, new SabertoothNishoba());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(nishoba.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nishoba);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Viashino Grappler");
    }

    @Test
    void protectionPreventsBlueCombatDamageWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new VodalianMerchant());
        Permanent nishoba = addCreatureReady(player2, new SabertoothNishoba());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(nishoba.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nishoba);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Vodalian Merchant");
    }

    @Test
    void protectionDoesNotPreventGreenCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new LlanowarVanguard());
        Permanent nishoba = addCreatureReady(player2, new SabertoothNishoba());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(nishoba.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nishoba);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player1, "Llanowar Vanguard");
    }

    @Test
    void protectionPreventsRedSpellTargeting() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player2, new SabertoothNishoba());
        harness.setHand(player1, List.of(new ScorchingLava()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nishoba.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void protectionPreventsBlueSpellTargeting() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player2, new SabertoothNishoba());
        harness.setHand(player1, List.of(new Repulse()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nishoba.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void protectionPreventsControllersOwnRedAuraTargeting() {
        Permanent nishoba = harness.addToBattlefieldAndReturn(player1, new SabertoothNishoba());
        harness.setHand(player1, List.of(new ManiacalRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nishoba.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThat(gd.stack).isEmpty();
    }
}
