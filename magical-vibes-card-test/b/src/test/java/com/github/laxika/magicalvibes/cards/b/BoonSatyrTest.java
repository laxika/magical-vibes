package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoonSatyr.class, GrizzlyBears.class, NessianCourser.class, VoyagesEnd.class})
class BoonSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("Boon Satyr can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent satyr = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, satyr)).isTrue();
    }

    @Test
    @DisplayName("Boon Satyr can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent satyr = findPermanent(player1, "Boon Satyr");
        assertThat(gqs.isCreature(gd, satyr)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("A bestowed Boon Satyr becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent satyr = findPermanent(player1, "Boon Satyr");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(satyr);
        assertThat(gqs.isCreature(gd, satyr)).isTrue();
        assertThat(satyr.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Flash allows normal casting during the opponent's combat")
    void castsCreatureDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent satyr = findPermanent(player1, "Boon Satyr");
        assertThat(gqs.isCreature(gd, satyr)).isTrue();
        assertThat(satyr.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows bestow during the opponent's combat onto their creature")
    void bestowsDuringOpponentsCombatOntoOpponentsCreature() {
        Permanent courser = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castWithAlternateCost(player1, 0, courser.getId());
        harness.passBothPriorities();

        Permanent satyr = findPermanent(player1, "Boon Satyr");
        assertThat(gqs.isCreature(gd, satyr)).isFalse();
        assertThat(satyr.getAttachedTo()).isEqualTo(courser.getId());
        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(5);
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target is bounced in response")
    void resolvesAsCreatureWhenBestowTargetLeaves() {
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castWithAlternateCost(player1, 0, courser.getId());
        harness.castInstant(player2, 0, courser.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Nessian Courser");
        Permanent satyr = findPermanent(player1, "Boon Satyr");
        assertThat(gqs.isCreature(gd, satyr)).isTrue();
        assertThat(satyr.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, satyr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, satyr)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Boon Satyr");
    }

    @Test
    @DisplayName("Bestow requires five mana even when the normal creature cost is affordable")
    void cannotBestowWithOnlyFourMana() {
        Permanent courser = harness.addToBattlefieldAndReturn(player1, new NessianCourser());
        harness.setHand(player1, List.of(new BoonSatyr()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, courser.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Boon Satyr");
        harness.assertNotOnBattlefield(player1, "Boon Satyr");
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, courser)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, courser)).isEqualTo(3);
    }
}
