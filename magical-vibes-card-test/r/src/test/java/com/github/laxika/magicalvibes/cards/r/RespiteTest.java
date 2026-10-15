package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.l.LowlandGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBlast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Respite.class, HornedTurtle.class, LowlandGiant.class, LightningBlast.class})
class RespiteTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all combat damage and gains 1 life per attacking creature")
    void preventsCombatDamageAndGainsLifePerAttacker() {
        addCreatureReady(player1, new HornedTurtle());
        addCreatureReady(player1, new HornedTurtle());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Respite()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0, 1));
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);

        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Gains no life when no creature is attacking")
    void gainsNoLifeWithoutAttackers() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new Respite(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Respite");
    }

    @Test
    @DisplayName("Counts attacking creatures controlled by the caster")
    void countsAttackersControlledByCaster() {
        addCreatureReady(player1, new HornedTurtle());
        addCreatureReady(player1, new HornedTurtle());
        addCreatureReady(player1, new HornedTurtle());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Respite()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0, 2));
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Prevents combat damage from both attackers and blockers")
    void preventsCombatDamageFromAttackersAndBlockers() {
        addCreatureReady(player1, new LowlandGiant());
        addCreatureReady(player2, new LowlandGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Respite()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        harness.assertOnBattlefield(player1, "Lowland Giant");
        harness.assertOnBattlefield(player2, "Lowland Giant");
    }

    @Test
    @DisplayName("Counts only creatures still attacking when Respite resolves")
    void countsAttackersAtResolution() {
        var attacker = addCreatureReady(player1, new HornedTurtle());
        addCreatureReady(player1, new HornedTurtle());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Respite(), new LightningBlast()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        declareAttackers(player1, List.of(0, 1));
        harness.castInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0, attacker.getId());
        harness.assertInGraveyard(player1, "Horned Turtle");
        harness.passBothPriorities();

        harness.assertLife(player2, 21);
        resolveCombat(player1);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LightningBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player2, new Respite(), "{1}{G}");
        harness.passBothPriorities();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 16);
    }
}
