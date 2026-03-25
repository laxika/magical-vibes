package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesOfChosenSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VanquishersBannerTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, int power, int toughness,
                                       CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    // ===== Card structure =====

    @Test
    @DisplayName("Has ChooseSubtypeOnEnterEffect, BoostCreaturesOfChosenSubtypeEffect, and ChosenSubtypeSpellCastTriggerEffect")
    void hasCorrectEffects() {
        VanquishersBanner card = new VanquishersBanner();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseSubtypeOnEnterEffect.class);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(BoostCreaturesOfChosenSubtypeEffect.class);
        BoostCreaturesOfChosenSubtypeEffect boost =
                (BoostCreaturesOfChosenSubtypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(boost.powerBoost()).isEqualTo(1);
        assertThat(boost.toughnessBoost()).isEqualTo(1);

        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst())
                .isInstanceOf(ChosenSubtypeSpellCastTriggerEffect.class);
        ChosenSubtypeSpellCastTriggerEffect trigger =
                (ChosenSubtypeSpellCastTriggerEffect) card.getEffects(EffectSlot.ON_CONTROLLER_CASTS_SPELL).getFirst();
        assertThat(trigger.resolvedEffects()).hasSize(1);
        assertThat(trigger.resolvedEffects().getFirst()).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Entering the battlefield =====

    @Test
    @DisplayName("Casting and resolving Vanquisher's Banner prompts for creature type choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new VanquishersBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vanquisher's Banner"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("Choosing a creature type sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new VanquishersBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ELF");

        Permanent banner = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Vanquisher's Banner"))
                .findFirst().orElseThrow();
        assertThat(banner.getChosenSubtype()).isEqualTo(CardSubtype.ELF);
    }

    // ===== Static +1/+1 boost =====

    @Test
    @DisplayName("Creatures you control of the chosen type get +1/+1")
    void boostsCreaturesOfChosenType() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.addToBattlefield(player1, elf);

        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures of a different type do not get the boost")
    void doesNotBoostDifferentType() {
        Card goblin = createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN, CardSubtype.WARRIOR);
        harness.addToBattlefield(player1, goblin);

        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        Permanent goblinPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type do not get the boost")
    void doesNotBoostOpponentCreatures() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player2, elf);

        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        Permanent elfPerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("No boost if no creature type was chosen yet")
    void noBoostWithoutChoice() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);

        // Add banner without setting chosen subtype
        harness.addToBattlefield(player1, new VanquishersBanner());

        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    // ===== Cast trigger: draw a card =====

    @Test
    @DisplayName("Casting a creature of the chosen type triggers draw a card")
    void castingChosenTypeCreatureTriggersDrawCard() {
        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
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
        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
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
        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        Card goblin = createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN, CardSubtype.WARRIOR);
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
        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
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

        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        // Only the creature spell on stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Boost removed when Banner leaves =====

    @Test
    @DisplayName("Boost is removed when Vanquisher's Banner leaves the battlefield")
    void boostRemovedWhenBannerLeaves() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);

        VanquishersBanner bannerCard = new VanquishersBanner();
        Permanent bannerPerm = new Permanent(bannerCard);
        bannerPerm.setChosenSubtype(CardSubtype.ELF);
        gd.playerBattlefields.get(player1.getId()).add(bannerPerm);

        // Verify boost is applied
        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(1);

        // Remove the banner
        gd.playerBattlefields.get(player1.getId()).remove(bannerPerm);

        // Boost should be gone
        assertThat(gqs.computeStaticBonus(gd, elfPerm).power()).isEqualTo(0);
    }

    // ===== Full integration test =====

    @Test
    @DisplayName("Full flow: cast Banner, choose type, creature gets boost, casting creature draws a card")
    void fullIntegrationTest() {
        Card elfOnBattlefield = createCreature("Elvish Mystic", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.addToBattlefield(player1, elfOnBattlefield);

        harness.setHand(player1, List.of(new VanquishersBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Cast and resolve Banner
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Choose Elf
        harness.handleListChoice(player1, "ELF");

        // Verify creature gets +1/+1
        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elvish Mystic"))
                .findFirst().orElseThrow();
        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);

        // Now cast another Elf creature and verify draw trigger
        Card anotherElf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.setHand(player1, List.of(anotherElf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        // Resolve the triggered ability (draw)
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }
}
