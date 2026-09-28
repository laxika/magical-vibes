package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFugitiveDoctor.class, Shock.class, GrizzlyBears.class})
class TheFugitiveDoctorTest extends BaseCardTest {

    @Test
    void entersAndInvestigates() {
        castDoctor();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void attackingMaySacrificeClueToGrantFixedCostFlashback() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sacrificeChoice.validIds()).containsExactly(findPermanents(player1, "Clue").getFirst().getId());
        harness.handlePermanentChosen(player1, sacrificeChoice.validIds().iterator().next());

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(targetChoice.validCardIds()).containsExactly(shock.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.cardsGrantedFlashbackCostsUntilEndOfTurn).containsEntry(shock.getId(), "{2}{R}{G}");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void decliningAttackTriggerKeepsClue() {
        Permanent doctor = castDoctor();
        doctor.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private Permanent castDoctor() {
        harness.setHand(player1, List.of(new TheFugitiveDoctor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "The Fugitive Doctor");
    }
}
