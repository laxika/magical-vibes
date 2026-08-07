package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieryImpulseTest extends BaseCardTest {

    @Test
    @DisplayName("Without spell mastery it deals only 2 damage")
    void dealsTwoDamageWithoutSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new Shock()));

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(permanentOf(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("With spell mastery it deals 3 damage instead, killing a 3/3")
    void dealsThreeDamageWithSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new Shock(), new LightningBolt()));

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Creature cards in the graveyard do not enable spell mastery")
    void creatureCardsDoNotEnableSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(permanentOf(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature its controller owns")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private Permanent permanentOf(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
