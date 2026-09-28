package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.b.BrilliantHalo;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MantleOfTheAncients.class, GrizzlyBears.class, BrilliantHalo.class, Bonesplitter.class})
class MantleOfTheAncientsTest extends BaseCardTest {

    @Test
    void returnsTargetedAurasAndEquipmentAttachedToEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        BrilliantHalo aura = new BrilliantHalo();
        Bonesplitter equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(aura, equipment, new GrizzlyBears()));
        harness.setHand(player1, List.of(new MantleOfTheAncients()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = harness.getGameData().interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(aura.getId(), equipment.getId());
        assertThat(choice.minCount()).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId(), equipment.getId()));
        harness.passBothPriorities();

        Permanent returnedAura = findPermanent(player1, "Brilliant Halo");
        Permanent returnedEquipment = findPermanent(player1, "Bonesplitter");
        assertThat(returnedAura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(returnedEquipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(harness.getGameQueryService().getEffectivePower(harness.getGameData(), creature)).isEqualTo(8);
        assertThat(harness.getGameQueryService().getEffectiveToughness(harness.getGameData(), creature)).isEqualTo(7);
    }
}
