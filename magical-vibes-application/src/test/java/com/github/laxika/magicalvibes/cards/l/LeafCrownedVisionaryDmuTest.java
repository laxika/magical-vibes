package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeafCrownedVisionary.class, LlanowarElves.class, GrizzlyBears.class, Forest.class})
class LeafCrownedVisionaryDmuTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elves you control get +1/+1")
    void buffsOtherElvesYouControl() {
        harness.addToBattlefield(player1, new LlanowarElves());
        Permanent elves = findPermanent(player1, "Llanowar Elves");
        int basePower = gqs.getEffectivePower(gd, elves);
        int baseToughness = gqs.getEffectiveToughness(gd, elves);

        harness.addToBattlefield(player1, new LeafCrownedVisionary());

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(baseToughness + 1);
    }

    @Test
    @DisplayName("The Visionary does not buff itself or non-Elves")
    void doesNotBuffItselfOrNonElves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        Permanent visionary = findPermanent(player1, "Leaf-Crowned Visionary");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(baseToughness);
        assertThat(gqs.getEffectivePower(gd, visionary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, visionary)).isEqualTo(1);
    }

    @Test
    @DisplayName("Paying {G} after casting an Elf draws a card")
    void payingAfterCastingElfDrawsCard() {
        Forest drawnCard = new Forest();
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Leaf-Crowned Visionary"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Declining the payment after casting an Elf draws nothing")
    void decliningAfterCastingElfDrawsNothing() {
        Forest drawnCard = new Forest();
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Casting a non-Elf spell does not trigger the draw ability")
    void nonElfSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Leaf-Crowned Visionary"));
    }
}
