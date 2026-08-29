package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({WhirlwindDenial.class, GrizzlyBears.class, IcyManipulator.class, LightningBolt.class})
class WhirlwindDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an opponent's spell and activated ability when they cannot pay")
    void countersOpponentSpellAndAbilityWithoutPayment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player2, "Icy Manipulator");
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

        harness.setHand(player1, List.of(new WhirlwindDenial()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0);
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

        harness.setHand(player1, List.of(new WhirlwindDenial()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0);
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
}
