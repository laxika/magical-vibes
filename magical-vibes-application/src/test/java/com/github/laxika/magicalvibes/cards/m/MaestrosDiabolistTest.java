package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DanceWithDevils;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({MaestrosDiabolist.class, DanceWithDevils.class, DoomBlade.class, GrizzlyBears.class})
class MaestrosDiabolistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking without a Devil token creates a tapped and attacking Devil")
    void attackCreatesTappedAttackingDevil() {
        addCreatureReady(player1, new MaestrosDiabolist());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent devil = findPermanents(player1, "Devil").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(devil.getCard().getPower()).isEqualTo(1);
        assertThat(devil.getCard().getToughness()).isEqualTo(1);
        assertThat(devil.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(devil.getCard().getSubtypes()).containsExactly(CardSubtype.DEVIL);
        assertThat(devil.isTapped()).isTrue();
        assertThat(devil.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Controlling a Devil token stops the attack trigger")
    void existingDevilTokenStopsTrigger() {
        addCreatureReady(player1, new MaestrosDiabolist());
        harness.setHand(player1, List.of(new DanceWithDevils()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Devil")).hasSize(2);

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Devil")).hasSize(2);
    }

    @Test
    @DisplayName("The created Devil deals 1 damage to any target when it dies")
    void devilDealsDamageWhenItDies() {
        addCreatureReady(player1, new MaestrosDiabolist());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        Permanent devil = findPermanents(player1, "Devil").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, devil.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }
}
