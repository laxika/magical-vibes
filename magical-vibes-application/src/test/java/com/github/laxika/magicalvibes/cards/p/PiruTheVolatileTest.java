package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KokushoTheEveningStar;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PiruTheVolatile.class, GrizzlyBears.class, KokushoTheEveningStar.class, Murder.class})
class PiruTheVolatileTest extends BaseCardTest {

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Piru")
    void decliningUpkeepPaymentSacrificesPiru() {
        harness.addToBattlefield(player1, new PiruTheVolatile());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Piru, the Volatile");
    }

    @Test
    @DisplayName("Paying {R}{W}{B} during upkeep keeps Piru")
    void payingUpkeepPaymentKeepsPiru() {
        harness.addToBattlefield(player1, new PiruTheVolatile());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Piru, the Volatile");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Piru deals 7 damage only to nonlegendary creatures when it dies")
    void deathTriggerDamagesNonlegendaryCreaturesOnly() {
        Permanent piru = harness.addToBattlefieldAndReturn(player1, new PiruTheVolatile());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new KokushoTheEveningStar());

        destroy(player2, piru);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Piru, the Volatile");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(legendary.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Kokusho, the Evening Star");
    }

    private void destroy(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Murder()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
