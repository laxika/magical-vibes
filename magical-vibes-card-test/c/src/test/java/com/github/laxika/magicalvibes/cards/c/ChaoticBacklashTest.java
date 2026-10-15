package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BallynockTrapper;
import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.c.CreakwoodGhoul;
import com.github.laxika.magicalvibes.cards.f.FableOfWolfAndOwl;
import com.github.laxika.magicalvibes.cards.i.IndigoFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChaoticBacklash.class, BallynockTrapper.class, IndigoFaerie.class,
        FableOfWolfAndOwl.class, CreakwoodGhoul.class, CinderPyromancer.class})
class ChaoticBacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice the number of white and/or blue permanents the target controls")
    void dealsTwiceWhiteAndBluePermanents() {
        // Target controls 1 white creature, 1 blue creature, and 1 blue-green enchantment = 3,
        // doubled = 6 damage. The multicolored permanent counts only once.
        addCreatureReady(player2, new BallynockTrapper());
        addCreatureReady(player2, new IndigoFaerie());
        harness.addToBattlefield(player2, new FableOfWolfAndOwl());

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14); // 20 - 6
    }

    @Test
    @DisplayName("Does not count permanents that are neither white nor blue")
    void ignoresOtherColors() {
        addCreatureReady(player2, new CreakwoodGhoul());    // black
        addCreatureReady(player2, new CinderPyromancer());  // red
        addCreatureReady(player2, new BallynockTrapper());  // white

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Only the white permanent counts = 1, doubled = 2 damage.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18); // 20 - 2
    }

    @Test
    @DisplayName("Deals no damage when target controls no white or blue permanents")
    void dealsNoDamageWithNoWhiteOrBlue() {
        addCreatureReady(player2, new CreakwoodGhoul());

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts permanents controlled by the target player, not the caster")
    void countsTargetPlayersPermanents() {
        addCreatureReady(player1, new BallynockTrapper());
        addCreatureReady(player2, new CreakwoodGhoul());

        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Counts qualifying permanents when the spell resolves")
    void countsAtResolution() {
        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player2, new BallynockTrapper());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A permanent that is both white and blue counts only once")
    void countsWhiteBluePermanentOnce() {
        var trapper = harness.addToBattlefieldAndReturn(player2, new BallynockTrapper());
        harness.addToBattlefield(player1, new IndigoFaerie());
        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, trapper.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target the caster and counts that player's permanents")
    void canTargetCaster() {
        harness.addToBattlefield(player1, new BallynockTrapper());
        harness.addToBattlefield(player1, new IndigoFaerie());
        harness.addToBattlefield(player2, new BallynockTrapper());
        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("White and blue cards outside the battlefield do not count")
    void ignoresCardsOutsideBattlefield() {
        harness.setHand(player2, List.of(new BallynockTrapper()));
        harness.setGraveyard(player2, List.of(new IndigoFaerie()));
        harness.setExile(player2, List.of(new FableOfWolfAndOwl()));
        harness.setHand(player1, List.of(new ChaoticBacklash()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }
}
