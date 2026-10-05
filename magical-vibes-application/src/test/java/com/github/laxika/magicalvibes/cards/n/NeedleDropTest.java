package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.l.LowlandOaf;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeedleDrop.class, WoodlandChangeling.class, LowlandOaf.class, Tarfire.class})
class NeedleDropTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a creature that was dealt damage this turn and draws a card")
    void dealsDamageToDamagedCreatureAndDraws() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Woodland Changeling");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a player that was dealt damage this turn and draws a card")
    void dealsDamageToDamagedPlayerAndDraws() {
        harness.setLife(player2, 20);
        gd.playersDealtDamageThisTurn.add(player2.getId());

        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        // A damaged Lowland Oaf provides a legal target so the spell is castable;
        // the Woodland Changeling we actually target was not dealt damage and must be rejected.
        gd.permanentsDealtDamageThisTurn.add(harness.addToBattlefieldAndReturn(player2, new LowlandOaf()).getId());
        UUID undamagedTargetId = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling()).getId();

        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, undamagedTargetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a player that was not dealt damage this turn")
    void cannotTargetUndamagedPlayer() {
        // A damaged Lowland Oaf makes the spell castable; player2 took no damage and is illegal.
        gd.permanentsDealtDamageThisTurn.add(harness.addToBattlefieldAndReturn(player2, new LowlandOaf()).getId());

        harness.setHand(player1, List.of(new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Spell damage enables Needle Drop and lethal damage still draws")
    void spellDamageEnablesTargetAndLethalDamageStillDraws() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LowlandOaf()).getId();
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setHand(player1, List.of(new Tarfire(), new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Lowland Oaf").getMarkedDamage()).isEqualTo(2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Lowland Oaf");
        harness.assertInGraveyard(player2, "Lowland Oaf");
        harness.assertInHand(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Can target its controller after spell damage")
    void canTargetDamagedController() {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setHand(player1, List.of(new Tarfire(), new NeedleDrop()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertInHand(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Does not draw when its only target dies in response")
    void doesNotDrawWhenTargetDiesInResponse() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LowlandOaf()).getId();
        harness.setLibrary(player1, List.of(new WoodlandChangeling()));
        harness.setHand(player1, List.of(new Tarfire(), new NeedleDrop(), new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Lowland Oaf");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Needle Drop");
    }

}
