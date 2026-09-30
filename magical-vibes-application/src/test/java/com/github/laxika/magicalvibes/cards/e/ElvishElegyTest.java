package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PerpetualPowerToughnessModifier;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishElegy.class, Forest.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class ElvishElegyTest extends BaseCardTest {

    @Test
    void millsBoostsAllGraveyardCreaturesAndMayReturnAnElfOrLand() {
        Card existingCreature = new GrizzlyBears();
        LlanowarElves elf = new LlanowarElves();
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(existingCreature));
        harness.setLibrary(player1, List.of(elf, forest, new Shock()));
        harness.setHand(player1, List.of(new ElvishElegy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(existingCreature, elf, forest);
        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(existingCreature.getId(), new PerpetualPowerToughnessModifier(1, 1))
                .containsEntry(elf.getId(), new PerpetualPowerToughnessModifier(1, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(elf);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
    }

    @Test
    void decliningTheOptionalReturnLeavesTheMilledLandInTheGraveyard() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock()));
        harness.setHand(player1, List.of(new ElvishElegy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }
}
