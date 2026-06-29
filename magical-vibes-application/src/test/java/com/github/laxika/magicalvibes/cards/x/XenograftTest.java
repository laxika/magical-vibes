package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XenograftTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Xenograft has correct effects")
    void hasCorrectEffects() {
        Xenograft card = new Xenograft();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseSubtypeOnEnterEffect.class);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantChosenSubtypeToOwnCreaturesEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Xenograft puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Xenograft");
    }

    @Test
    @DisplayName("Resolving Xenograft enters battlefield and awaits creature type choice")
    void resolvingTriggersSubtypeChoice() {
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Xenograft"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a creature type sets chosenSubtype on Xenograft")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        Permanent xenograft = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Xenograft"))
                .findFirst().orElseThrow();
        assertThat(xenograft.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    // ===== Static effect: grant chosen subtype to own creatures =====

    @Test
    @DisplayName("Creatures you control gain the chosen creature type")
    void grantsChosenSubtypeToOwnCreatures() {
        Card bear = createCreature("Bear Cub", 2, 2, CardColor.GREEN, CardSubtype.BEAR);
        harness.addToBattlefield(player1, bear);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = new Permanent(xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);
        gd.playerBattlefields.get(player1.getId()).add(xenograftPerm);

        Permanent bearPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bear Cub"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures retain their original types in addition to the chosen type")
    void creaturesRetainOriginalSubtypes() {
        Card elf = createCreature("Llanowar Elves", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.addToBattlefield(player1, elf);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = new Permanent(xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.WIZARD);
        gd.playerBattlefields.get(player1.getId()).add(xenograftPerm);

        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.WIZARD);
        // Original subtypes remain on the card itself
        assertThat(elfPerm.getCard().getSubtypes()).contains(CardSubtype.ELF, CardSubtype.DRUID);
    }

    @Test
    @DisplayName("Opponent's creatures do not gain the chosen type")
    void doesNotAffectOpponentCreatures() {
        Card opponentCreature = createCreature("Grizzly Bears", 2, 2, CardColor.GREEN, CardSubtype.BEAR);
        harness.addToBattlefield(player2, opponentCreature);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = new Permanent(xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);
        gd.playerBattlefields.get(player1.getId()).add(xenograftPerm);

        Permanent bearPerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("No subtype granted if no creature type was chosen yet")
    void noSubtypeWithoutChoice() {
        Card bear = createCreature("Bear Cub", 2, 2, CardColor.GREEN, CardSubtype.BEAR);
        harness.addToBattlefield(player1, bear);

        // Add Xenograft without setting chosen subtype
        harness.addToBattlefield(player1, new Xenograft());

        Permanent bearPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bear Cub"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Creature already of the chosen type does not get duplicate subtype")
    void noDuplicateSubtype() {
        Card goblin = createCreature("Goblin Piker", 2, 1, CardColor.RED, CardSubtype.GOBLIN, CardSubtype.WARRIOR);
        harness.addToBattlefield(player1, goblin);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = new Permanent(xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);
        gd.playerBattlefields.get(player1.getId()).add(xenograftPerm);

        Permanent goblinPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        // GrantedSubtypes should contain GOBLIN at most once (accumulator deduplicates)
        assertThat(bonus.grantedSubtypes().stream().filter(s -> s == CardSubtype.GOBLIN).count()).isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Non-creature permanents are not affected")
    void doesNotAffectNonCreatures() {
        Card artifact = new Card();
        artifact.setName("Sol Ring");
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("{1}");
        harness.addToBattlefield(player1, artifact);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = new Permanent(xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);
        gd.playerBattlefields.get(player1.getId()).add(xenograftPerm);

        Permanent artifactPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sol Ring"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, artifactPerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    // ===== Full integration test =====

    @Test
    @DisplayName("Full flow: cast, resolve, choose creature type, creatures gain type")
    void fullIntegrationTest() {
        Card bear = createCreature("Bear Cub", 2, 2, CardColor.GREEN, CardSubtype.BEAR);
        harness.addToBattlefield(player1, bear);

        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Cast and resolve Xenograft
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        // Choose Goblin
        harness.handleListChoice(player1, "GOBLIN");

        // Verify creature gains the chosen type
        Permanent bearPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bear Cub"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);

        // Verify the Xenograft permanent has the chosen subtype stored
        Permanent xenograft = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Xenograft"))
                .findFirst().orElseThrow();
        assertThat(xenograft.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }
}
