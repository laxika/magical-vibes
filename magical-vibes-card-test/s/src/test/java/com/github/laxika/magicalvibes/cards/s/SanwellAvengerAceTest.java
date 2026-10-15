package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.cards.p.PeacewalkerColossus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanwellAvengerAce.class, PeacewalkerColossus.class, JhoirasFamiliar.class,
        GrizzlyBears.class, Shock.class})
class SanwellAvengerAceTest extends BaseCardTest {

    @Test
    void tapTriggerOffersVehiclesAndArtifactCreaturesOnly() {
        PeacewalkerColossus vehicle = new PeacewalkerColossus();
        JhoirasFamiliar artifactCreature = new JhoirasFamiliar();
        harness.setLibrary(player1, List.of(
                vehicle, artifactCreature,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        Permanent sanwell = addCreatureReady(player1, new SanwellAvengerAce());

        PendingInteraction.ImprovisationCapstoneCastChoice choice = triggerSanwell(sanwell);

        assertThat(choice.validCardIds()).containsExactly(vehicle.getId(), artifactCreature.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    void chosenVehicleIsCastWithoutPayingMana() {
        PeacewalkerColossus vehicle = new PeacewalkerColossus();
        harness.setLibrary(player1, List.of(
                vehicle,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        Permanent sanwell = addCreatureReady(player1, new SanwellAvengerAce());

        PendingInteraction.ImprovisationCapstoneCastChoice choice = triggerSanwell(sanwell);
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(vehicle.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    void preventsDamageWhileAnArtifactCreatureAttacks() {
        Permanent sanwell = addCreatureReady(player1, new SanwellAvengerAce());
        Permanent artifactCreature = addCreatureReady(player1, new JhoirasFamiliar());
        artifactCreature.setAttacking(true);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, sanwell.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(sanwell.getId()));
    }

    private PendingInteraction.ImprovisationCapstoneCastChoice triggerSanwell(Permanent sanwell) {
        sanwell.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, sanwell));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        return gd.interaction.activeInteraction(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }
}
