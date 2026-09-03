package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Delirium;
import com.github.laxika.magicalvibes.cards.m.MtendaGriffin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberwildeCaliph.class, Delirium.class, MtendaGriffin.class})
class EmberwildeCaliphTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes its controller lose that much life")
    void combatDamageToPlayerLosesLife() {
        addCreatureReady(player1, new EmberwildeCaliph());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16); // 4 combat damage
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16); // lost that much life
    }

    @Test
    @DisplayName("Loses no life when it deals no damage")
    void noLifeLossWithoutDamage() {
        EmberwildeCaliph caliph = new EmberwildeCaliph();
        caliph.setPower(0);
        addCreatureReady(player1, caliph);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Must be declared as an attacker when able")
    void mustAttackWhenAble() {
        addCreatureReady(player1, new EmberwildeCaliph());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Combat damage to a blocker and excess trample damage both count")
    void combatDamageToBlockerAndExcessTrampleDamageBothCount() {
        addCreatureReady(player1, new EmberwildeCaliph());
        Permanent blocker = addCreatureReady(player2, new MtendaGriffin());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2
        ));
        resolveAllTriggers();

        harness.assertLife(player1, 16); // 4 total combat damage dealt by Emberwilde Caliph
        harness.assertLife(player2, 18); // 2 trample damage
        harness.assertInGraveyard(player2, "Mtenda Griffin");
    }

    @Test
    @DisplayName("Its controller loses life when it deals damage before dying")
    void controllerLosesLifeWhenCaliphDiesAfterDealingDamage() {
        addCreatureReady(player1, new EmberwildeCaliph());
        Permanent blocker = addCreatureReady(player2, new EmberwildeCaliph());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Emberwilde Caliph");
        harness.assertInGraveyard(player2, "Emberwilde Caliph");
    }

    @Test
    @DisplayName("Noncombat damage also makes its controller lose that much life")
    void noncombatDamageAlsoMakesControllerLoseLife() {
        harness.forceActivePlayer(player2);
        harness.setLife(player2, 20);
        Permanent caliph = addCreatureReady(player2, new EmberwildeCaliph());
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, caliph.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player2, 12); // 4 damage plus 4 life lost from Emberwilde Caliph
    }
}
