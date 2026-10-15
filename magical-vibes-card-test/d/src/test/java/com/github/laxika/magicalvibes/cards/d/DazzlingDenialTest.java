package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.a.AvenSquire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DazzlingDenial.class, AngelsMercy.class, AvenSquire.class})
class DazzlingDenialTest extends BaseCardTest {

    @Test
    void countersUnlessControllerPaysTwo() {
        AngelsMercy angelsMercy = castTargetSpell();
        castDazzlingDenial(angelsMercy);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 17);
    }

    @Test
    void usesFourWhenControllerControlsABird() {
        harness.addToBattlefield(player2, new AvenSquire());
        AngelsMercy angelsMercy = castTargetSpell();
        castDazzlingDenial(angelsMercy);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 10);
    }

    @Test
    void countersWhenControllerDeclinesToPayTwo() {
        AngelsMercy angelsMercy = castTargetSpell();
        castDazzlingDenial(angelsMercy);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 10);
    }

    @Test
    void allowsSpellToResolveWhenControllerPaysFour() {
        harness.addToBattlefield(player2, new AvenSquire());
        AngelsMercy angelsMercy = castTargetSpell();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        castDazzlingDenial(angelsMercy);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 17);
    }

    @Test
    void opponentsBirdDoesNotIncreasePayment() {
        harness.addToBattlefield(player1, new AvenSquire());
        AngelsMercy angelsMercy = castTargetSpell();
        castDazzlingDenial(angelsMercy);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    void checksForBirdAtResolutionRatherThanCasting() {
        AngelsMercy angelsMercy = castTargetSpell();
        harness.setHand(player2, List.of(new DazzlingDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, angelsMercy.getId());

        harness.addToBattlefield(player2, new AvenSquire());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Angel's Mercy");
        harness.assertLife(player1, 10);
    }

    private AngelsMercy castTargetSpell() {
        harness.setLife(player1, 10);
        AngelsMercy angelsMercy = new AngelsMercy();
        harness.castFromHand(player1, angelsMercy, "{2}{W}{W}");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);
        return angelsMercy;
    }

    private void castDazzlingDenial(AngelsMercy angelsMercy) {
        harness.setHand(player2, List.of(new DazzlingDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, angelsMercy.getId());
        harness.passBothPriorities();
    }
}
