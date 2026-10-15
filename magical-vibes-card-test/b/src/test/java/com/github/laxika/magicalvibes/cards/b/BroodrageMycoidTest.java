package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CompassGnome;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroodrageMycoid.class, ZuranOrb.class, Forest.class, CompassGnome.class, DeadWeight.class})
class BroodrageMycoidTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fungus token that can't block at your end step after descending")
    void createsNonBlockingFungusAfterDescending() {
        harness.addToBattlefield(player1, new BroodrageMycoid());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        List<Permanent> fungus = findPermanents(player1, "Fungus");
        assertThat(fungus).hasSize(1).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.FUNGUS);
            assertThat(bls.canBlock(gd, token)).isFalse();
        });
    }

    @Test
    @DisplayName("Does not create a Fungus token at your end step without descending")
    void doesNotCreateFungusWithoutDescending() {
        harness.addToBattlefield(player1, new BroodrageMycoid());

        advanceToEndStep(player1);

        assertThat(findPermanents(player1, "Fungus")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Descending multiple times creates only one token")
    void multipleDescentsCreateOneToken() {
        harness.addToBattlefield(player1, new BroodrageMycoid());
        killGnome(player1);
        killGnome(player1);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).hasSize(1).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(bls.canBlock(gd, token)).isFalse();
        });
    }

    @Test
    @DisplayName("An opponent descending does not satisfy your end step condition")
    void opponentsDescentDoesNotCount() {
        harness.addToBattlefield(player1, new BroodrageMycoid());
        killGnome(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Fungus")).isEmpty();
    }

    @Test
    @DisplayName("Descending does not trigger Mycoid during the opponent's end step")
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new BroodrageMycoid());
        killGnome(player1);

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Fungus")).isEmpty();
    }

    @Test
    @DisplayName("Descending after the end step begins does not retroactively trigger Mycoid")
    void descendingTooLateDoesNotTrigger() {
        harness.addToBattlefield(player1, new BroodrageMycoid());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Fungus")).isEmpty();
    }

    private void killGnome(Player owner) {
        Permanent gnome = harness.addToBattlefieldAndReturn(owner, new CompassGnome());
        harness.forceActivePlayer(owner);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(owner, List.of(new DeadWeight()));
        harness.addMana(owner, ManaColor.BLACK, 1);
        harness.castEnchantment(owner, 0, gnome.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(owner, "Compass Gnome");
    }
    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
