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
        Permanent visionary = harness.addToBattlefieldAndReturn(player1, new LeafCrownedVisionary());
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent nonElf = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());

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

    @Test
    @DisplayName("Two Visionaries boost each other and their bonuses stack on another Elf")
    void multipleVisionariesBoostOtherElves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeafCrownedVisionary());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeafCrownedVisionary());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting Visionary without one already on the battlefield does not trigger itself")
    void castingVisionaryDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new LeafCrownedVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Casting another Visionary triggers the one already on the battlefield")
    void castingAnotherVisionaryTriggersExistingVisionary() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player1, List.of(new LeafCrownedVisionary()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("An opponent casting an Elf does not trigger your Visionary")
    void opponentElfSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the choice without enough green mana does not draw")
    void cannotDrawWithoutPayingGreenMana() {
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.addToBattlefield(player1, new LeafCrownedVisionary());
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).contains(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }
}
