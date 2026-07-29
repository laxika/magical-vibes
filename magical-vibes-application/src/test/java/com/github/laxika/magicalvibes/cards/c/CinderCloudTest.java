package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CinderCloudTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a white creature and deals damage equal to its power to its controller")
    void whiteCreatureDealsPowerDamage() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Serra Angel");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Destroys a non-white creature without dealing damage")
    void nonWhiteCreatureDealsNoDamage() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals no damage when the white creature survives destruction")
    void indestructibleWhiteCreatureTakesNoDamage() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new AdamantWill(), new CinderCloud()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new CinderCloud()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
