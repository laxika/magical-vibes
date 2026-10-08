package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VanquishersBanner.class, LlanowarElves.class, GoblinPiker.class, ElvishMystic.class})
class VanquishersBannerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting and resolving Vanquisher's Banner prompts for creature type choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new VanquishersBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vanquisher's Banner");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a creature type sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new VanquishersBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent banner = findPermanent(player1, "Vanquisher's Banner");
        assertThat(banner.getChosenSubtype()).isEqualTo(CardSubtype.ELF);
    }

    @Test
    @DisplayName("Creatures you control of the chosen type get +1/+1")
    void boostsCreaturesOfChosenType() {
        Card elf = new LlanowarElves();
        harness.addToBattlefield(player1, elf);

        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures of a different type do not get the boost")
    void doesNotBoostDifferentType() {
        Card goblin = new GoblinPiker();
        harness.addToBattlefield(player1, goblin);

        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        Permanent goblinPerm = findPermanent(player1, "Goblin Piker");

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type do not get the boost")
    void doesNotBoostOpponentCreatures() {
        Card elf = new LlanowarElves();
        harness.addToBattlefield(player2, elf);

        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        Permanent elfPerm = findPermanent(player2, "Llanowar Elves");

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost if no creature type was chosen yet")
    void noBoostWithoutChoice() {
        Card elf = new LlanowarElves();
        harness.addToBattlefield(player1, elf);

        // Add banner without setting chosen subtype
        harness.addToBattlefield(player1, new VanquishersBanner());

        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a creature of the chosen type triggers draw a card")
    void castingChosenTypeCreatureTriggersDrawCard() {
        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        Card elf = new LlanowarElves();
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        // Creature spell on stack + triggered ability
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Vanquisher's Banner"));
    }

    @Test
    @DisplayName("Resolving cast-triggered ability draws a card")
    void castTriggerDrawsCard() {
        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        Card elf = new LlanowarElves();
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        // Resolve the triggered ability (LIFO — trigger on top)
        harness.passBothPriorities();

        // Hand was 1 card, cast 1 (0 cards), then drew 1 card = 1 card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Casting a creature of a different type does not trigger draw")
    void castingDifferentTypeDoesNotTrigger() {
        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        Card goblin = new GoblinPiker();
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        // Only the creature spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Opponent casting a creature of the chosen type does not trigger controller's Banner")
    void opponentCastingDoesNotTrigger() {
        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card elf = new LlanowarElves();
        harness.setHand(player2, List.of(elf));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);

        // Only the creature spell on stack, no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("No trigger if no creature type was chosen yet")
    void noTriggerWithoutChoice() {
        // Add banner without setting chosen subtype
        harness.addToBattlefield(player1, new VanquishersBanner());

        Card elf = new LlanowarElves();
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        // Only the creature spell on stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Boost is removed when Vanquisher's Banner leaves the battlefield")
    void boostRemovedWhenBannerLeaves() {
        Card elf = new LlanowarElves();
        harness.addToBattlefield(player1, elf);

        Permanent bannerPerm = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        bannerPerm.setChosenSubtype(CardSubtype.ELF);

        // Verify boost is applied
        Permanent elfPerm = findPermanent(player1, "Llanowar Elves");
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(1);

        // Remove the banner
        gd.playerBattlefields.get(player1.getId()).remove(bannerPerm);

        // Boost should be gone
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each Banner independently boosts and triggers for a creature with both chosen types")
    void multipleBannersRecognizeDifferentSubtypesOfSameCreature() {
        Permanent elfBanner = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        elfBanner.setChosenSubtype(CardSubtype.ELF);
        Permanent druidBanner = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        druidBanner.setChosenSubtype(CardSubtype.DRUID);
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        var bonus = gqs.computeStaticBonus(gd, elf);
        assertThat(bonus.power()).isEqualTo(2);
        assertThat(bonus.toughness()).isEqualTo(2);

        harness.setLibrary(player1, List.of(new GoblinPiker(), new LlanowarElves()));
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A chosen-type creature entering without being cast does not draw a card")
    void enteringWithoutCastingDoesNotTrigger() {
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        banner.setChosenSubtype(CardSubtype.ELF);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        Permanent elf = harness.enterBattlefieldAndReturn(player1, new LlanowarElves());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gqs.computeStaticBonus(gd, elf).power()).isEqualTo(1);
    }

    @Test
    @DisplayName("The draw trigger still resolves after its Banner leaves the battlefield")
    void drawTriggerSurvivesBannerLeaving() {
        Permanent banner = harness.addToBattlefieldAndReturn(player1, new VanquishersBanner());
        banner.setChosenSubtype(CardSubtype.ELF);
        GoblinPiker drawnCard = new GoblinPiker();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);

        gd.playerBattlefields.get(player1.getId()).remove(banner);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Full flow: cast Banner, choose type, creature gets boost, casting creature draws a card")
    void fullIntegrationTest() {
        Card elfOnBattlefield = new ElvishMystic();
        harness.addToBattlefield(player1, elfOnBattlefield);

        harness.setHand(player1, List.of(new VanquishersBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Cast and resolve Banner
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Choose Elf
        harness.handleListChoice(player1, "ELF");

        // Verify creature gets +1/+1
        Permanent elfPerm = findPermanent(player1, "Elvish Mystic");
        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);

        // Now cast another Elf creature and verify draw trigger
        Card anotherElf = new LlanowarElves();
        harness.setHand(player1, List.of(anotherElf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        // Resolve the triggered ability (draw)
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }
}
