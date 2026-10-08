package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeightOfSpires.class, AssaultZeppelid.class, BreedingPool.class, Mountain.class})
class WeightOfSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the target creature controller's nonbasic lands")
    void dealsDamageForTargetControllersNonbasicLands() {
        harness.addToBattlefield(player1, new BreedingPool());
        harness.addToBattlefield(player1, new BreedingPool());
        harness.addToBattlefield(player2, new BreedingPool());
        harness.addToBattlefield(player2, new BreedingPool());
        harness.addToBattlefield(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts nonbasic lands added before the spell resolves")
    void countsNonbasicLandsAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.addToBattlefield(player2, new BreedingPool());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Basic lands do not increase the damage")
    void ignoresBasicLands() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }
    @Test
    @DisplayName("Can target your own creature and counts only your nonbasic lands")
    void canDamageOwnCreature() {
        harness.addToBattlefield(player1, new BreedingPool());
        harness.addToBattlefield(player2, new BreedingPool());
        harness.addToBattlefield(player2, new BreedingPool());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Nonland permanents do not increase damage")
    void ignoresNonlandPermanents() {
        harness.addToBattlefield(player2, new BreedingPool());
        harness.addToBattlefield(player2, new AssaultZeppelid());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Lethal damage puts the creature into its owner's graveyard")
    void lethalDamageDestroysCreature() {
        harness.addToBattlefield(player2, new BreedingPool());
        harness.addToBattlefield(player2, new BreedingPool());
        harness.addToBattlefield(player2, new BreedingPool());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new WeightOfSpires()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Assault Zeppelid");
        harness.assertInGraveyard(player2, "Assault Zeppelid");
        harness.assertInGraveyard(player1, "Weight of Spires");
    }
}
