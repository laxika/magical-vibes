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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
    }

    @Test
    void canDeclineTheElfAndReturnOnlyTheMilledLand() {
        LlanowarElves elf = new LlanowarElves();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(elf, forest, new Shock()));
        harness.setHand(player1, List.of(new ElvishElegy()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elf).doesNotContain(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void returnedElfRetainsItsBoostWhenCast() {
        LlanowarElves elf = new LlanowarElves();
        harness.setLibrary(player1, List.of(elf, new Shock(), new Shock()));
        harness.setHand(player1, List.of(new ElvishElegy()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(elf.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
    }

    @Test
    void boostsExistingCreaturesWithAnEmptyLibraryWithoutOfferingAnOldElf() {
        LlanowarElves elf = new LlanowarElves();
        LlanowarElves opposingElf = new LlanowarElves();
        harness.setGraveyard(player1, List.of(elf));
        harness.setGraveyard(player2, List.of(opposingElf));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ElvishElegy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(elf);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(elf.getId(), new PerpetualPowerToughnessModifier(1, 1))
                .doesNotContainKey(opposingElf.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void millsOnlyAvailableCardsAndDoesNotOfferANonElfCreature() {
        GrizzlyBears bear = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(bear, shock));
        harness.setHand(player1, List.of(new ElvishElegy()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear, shock);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.perpetualPowerToughnessModifiers)
                .containsEntry(bear.getId(), new PerpetualPowerToughnessModifier(1, 1))
                .doesNotContainKey(shock.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
