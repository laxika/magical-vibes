package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MasterSplicerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 3/3 colorless Phyrexian Golem artifact creature token")
    void etbCreatesGolemToken() {
        harness.setHand(player1, List.of(new MasterSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2); // Master Splicer + Golem token

        Permanent golemToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();
        assertThat(golemToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golemToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golemToken.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Golem token gets +1/+1 from Master Splicer's static ability, becoming 4/4")
    void golemTokenGetsPlusOnePlusOne() {
        harness.setHand(player1, List.of(new MasterSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, golemToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, golemToken)).isEqualTo(4);
    }

    @Test
    @DisplayName("Master Splicer itself does not get +1/+1 (not a Golem)")
    void masterSplicerDoesNotGetBoost() {
        harness.addToBattlefield(player1, new MasterSplicer());

        Permanent masterSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Master Splicer"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, masterSplicer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, masterSplicer)).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's Golems do not get +1/+1")
    void opponentGolemsDoNotGetBoost() {
        harness.addToBattlefield(player1, new MasterSplicer());

        // Put a Golem on the opponent's battlefield
        harness.setHand(player2, List.of(new MasterSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Player 2's Golem token should be 4/4 from player 2's own Master Splicer
        // but not get a second +1/+1 from player 1's Master Splicer
        Permanent p2Golem = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, p2Golem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, p2Golem)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost is lost when Master Splicer leaves the battlefield")
    void boostLostWhenMasterSplicerLeaves() {
        harness.setHand(player1, List.of(new MasterSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent golemToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Golem"))
                .findFirst()
                .orElseThrow();

        // Verify golem is 4/4 with Master Splicer
        assertThat(gqs.getEffectivePower(gd, golemToken)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, golemToken)).isEqualTo(4);

        // Remove Master Splicer from battlefield
        Permanent masterSplicer = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Master Splicer"))
                .findFirst()
                .orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(masterSplicer);

        // Golem should be back to base 3/3
        assertThat(gqs.getEffectivePower(gd, golemToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golemToken)).isEqualTo(3);
    }
}
