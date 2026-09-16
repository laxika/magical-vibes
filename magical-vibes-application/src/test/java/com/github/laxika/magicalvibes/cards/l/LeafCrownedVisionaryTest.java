package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeafCrownedVisionary.class, LlanowarElves.class, GrizzlyBears.class})
class LeafCrownedVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Other Elves you control get +1/+1, but the Visionary and opponents' Elves do not")
    void buffsOtherOwnElvesOnly() {
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent visionary = findPermanent(player1, "Leaf-Crowned Visionary");
        Permanent ownElf = findPermanent(player1, "Llanowar Elves");
        Permanent nonElf = findPermanent(player1, "Grizzly Bears");
        Permanent opponentElf = findPermanent(player2, "Llanowar Elves");

        assertThat(gqs.getEffectivePower(gd, visionary)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, visionary)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting an Elf offers to pay {G} to draw a card")
    void acceptingMayPayDrawsCard() {
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Declining the payment does not draw a card")
    void decliningMayPayDoesNotDraw() {
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Casting a non-Elf does not trigger the draw ability")
    void nonElfDoesNotTrigger() {
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
