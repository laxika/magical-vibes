package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandraFireOfKaladeshTest extends BaseCardTest {

    @Test
    @DisplayName("{T} pings a player for 1 and leaves Chandra untransformed below three damage")
    void pingsWithoutTransforming() {
        Permanent chandra = addReadyChandra(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, indexOf(player1, chandra), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        harness.assertOnBattlefield(player1, "Chandra, Fire of Kaladesh");
        harness.assertNotOnBattlefield(player1, "Chandra, Roaring Flame");
    }

    @Test
    @DisplayName("The third ping counts toward the three, so Chandra returns transformed")
    void transformsOnThirdDamage() {
        Permanent chandra = addReadyChandra(player1);

        for (int i = 0; i < 3; i++) {
            chandra.untap();
            harness.activateAbility(player1, indexOf(player1, chandra), null, player2.getId());
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Chandra, Fire of Kaladesh");
        harness.assertOnBattlefield(player1, "Chandra, Roaring Flame");

        Permanent walker = findPermanent(player1, "Chandra, Roaring Flame");
        assertThat(walker.isTransformed()).isTrue();
        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    @Test
    @DisplayName("Two pings are not enough — Chandra stays a creature")
    void doesNotTransformOnTwoDamage() {
        Permanent chandra = addReadyChandra(player1);

        for (int i = 0; i < 2; i++) {
            chandra.untap();
            harness.activateAbility(player1, indexOf(player1, chandra), null, player2.getId());
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Chandra, Fire of Kaladesh");
        harness.assertNotOnBattlefield(player1, "Chandra, Roaring Flame");
    }

    @Test
    @DisplayName("Damage dealt to a planeswalker also counts toward the three")
    void planeswalkerDamageCountsTowardTransform() {
        Permanent chandra = addReadyChandra(player1);
        Permanent nalaar = new Permanent(new ChandraNalaar());
        nalaar.setCounterCount(CounterType.LOYALTY, 6);
        gd.playerBattlefields.get(player2.getId()).add(nalaar);

        for (int i = 0; i < 3; i++) {
            chandra.untap();
            harness.activateAbility(player1, indexOf(player1, chandra), null, nalaar.getId());
            harness.passBothPriorities();
        }

        assertThat(nalaar.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Chandra, Roaring Flame");
    }

    @Test
    @DisplayName("Casting a red spell untaps Chandra")
    void redSpellUntapsChandra() {
        Permanent chandra = addReadyChandra(player1);

        harness.activateAbility(player1, indexOf(player1, chandra), null, player2.getId());
        harness.passBothPriorities();
        assertThat(chandra.isTapped()).isTrue();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a non-red spell leaves Chandra tapped")
    void nonRedSpellDoesNotUntapChandra() {
        Permanent chandra = addReadyChandra(player1);

        harness.activateAbility(player1, indexOf(player1, chandra), null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(chandra.isTapped()).isTrue();
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = new Permanent(new ChandraFireOfKaladesh());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
