package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThunderboltTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Mode 0 deals 3 damage to target player")
    void mode0DamagesPlayer() {
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player1, "Thunderbolt");
    }

    @Test
    @DisplayName("Mode 1 deals 4 damage to a creature with flying, killing it")
    void mode1KillsFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        UUID hawkId = battlefieldId("Suntail Hawk");
        harness.castInstant(player1, 0, 1, hawkId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Mode 1 deals exactly 4 damage to a large flier")
    void mode1DealsFourDamage() {
        SuntailHawk bigHawk = new SuntailHawk();
        bigHawk.setPower(5);
        bigHawk.setToughness(5);
        harness.addToBattlefield(player2, bigHawk);
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        UUID hawkId = battlefieldId("Suntail Hawk");
        harness.castInstant(player1, 0, 1, hawkId);
        harness.passBothPriorities();

        Permanent hawk = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(hawkId))
                .findFirst().orElseThrow();
        assertThat(hawk.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Mode 1 cannot target a creature without flying")
    void mode1CannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Thunderbolt()));
        giveMana();

        UUID bearsId = battlefieldId("Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID battlefieldId(String name) {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
    }
}
