package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.c.CrashingCentaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HowlingGale.class, AvenFlock.class, CrashingCentaur.class})
class HowlingGaleTest extends BaseCardTest {

    @Test
    @DisplayName("Howling Gale deals 1 damage to each player and each flying creature")
    void damagesPlayersAndFlyingCreatures() {
        Permanent ownFlyingCreature = harness.addToBattlefieldAndReturn(player1, new AvenFlock());
        Permanent opponentFlyingCreature = harness.addToBattlefieldAndReturn(player2, new AvenFlock());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new CrashingCentaur());
        harness.setHand(player1, List.of(new HowlingGale()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownFlyingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentFlyingCreature, groundCreature);
        assertThat(ownFlyingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(opponentFlyingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(groundCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A normal cast of Howling Gale goes to the graveyard")
    void normalCastGoesToGraveyard() {
        harness.setHand(player1, List.of(new HowlingGale()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertInGraveyard(player1, "Howling Gale");
    }

    @Test
    @DisplayName("Flashback casts Howling Gale and exiles it after resolution")
    void flashbackCastsAndExiles() {
        harness.setGraveyard(player1, List.of(new HowlingGale()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertNotInGraveyard(player1, "Howling Gale");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Howling Gale"));
    }

    @Test
    @DisplayName("Flashback requires its {1}{G} cost")
    void flashbackRequiresItsManaCost() {
        harness.setGraveyard(player1, List.of(new HowlingGale()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
