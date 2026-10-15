package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheSea;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhirlwindDenial.class, GrizzlyBears.class, IcyManipulator.class, LightningBolt.class,
        OmenOfTheSea.class, Island.class})
class WhirlwindDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's spell and activated ability when they cannot pay")
    void countersOpponentSpellAndAbilityWithoutPayment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        icyManipulator.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null,
                harness.getPermanentId(player1, "Grizzly Bears"));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Lets an opponent pay for one object and counters another")
    void paysForOnlyOneOpponentSpell() {
        LightningBolt first = new LightningBolt();
        LightningBolt second = new LightningBolt();
        harness.setHand(player2, List.of(first, second));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());

        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @CardUsed({WhirlwindDenial.class, OmenOfTheSea.class})
    void countersOpponentTriggeredAbility() {
        harness.castFromHand(player2, new OmenOfTheSea(), "{1}{U}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player2, "Omen of the Sea");
    }

    @Test
    @CardUsed({WhirlwindDenial.class, OmenOfTheSea.class})
    void countersActivatedAbilityAfterItsSourceIsSacrificed() {
        harness.addToBattlefield(player2, new OmenOfTheSea());
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.activateAbility(player2, 0, null, null);
        harness.assertInGraveyard(player2, "Omen of the Sea");

        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({WhirlwindDenial.class, OmenOfTheSea.class})
    void leavesControllersOwnSpellOnTheStack() {
        harness.castFromHand(player1, new OmenOfTheSea(), "{1}{U}");
        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Omen of the Sea");
    }

    @Test
    @CardUsed({WhirlwindDenial.class, OmenOfTheSea.class, Island.class})
    void offersPaymentWhenOpponentCanGenerateManaFromUntappedLands() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        harness.castFromHand(player2, new OmenOfTheSea(), "{1}{U}");
        harness.castFromHand(player1, new WhirlwindDenial(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        for (int i = 0; i < 4; i++) {
            harness.tapPermanent(player2, i);
        }
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player2, "Omen of the Sea");
    }
}
