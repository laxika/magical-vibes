package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeismicRupture.class, HillGiant.class, SuntailHawk.class, LlanowarElves.class})
class SeismicRuptureTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature without flying")
    void damagesCreaturesWithoutFlying() {
        Permanent ownGroundCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opposingGroundCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent opposingFlyingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SeismicRupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(ownGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingGroundCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(opposingFlyingCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not deal damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SeismicRupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal damage kills ground creatures on both sides but spares fragile flyers")
    void killsGroundCreaturesButSparesFlyers() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SeismicRupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }
}
