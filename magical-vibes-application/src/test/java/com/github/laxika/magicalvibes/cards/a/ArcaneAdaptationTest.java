package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.FieryCannonade;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.p.PryingBlade;
import com.github.laxika.magicalvibes.cards.r.RiggingRunner;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneAdaptation.class, JungleDelver.class, PryingBlade.class, RiggingRunner.class, FieryCannonade.class})
class ArcaneAdaptationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Arcane Adaptation puts it on the stack as an enchantment spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ArcaneAdaptation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(ArcaneAdaptation.class);
    }

    @Test
    @DisplayName("Resolving Arcane Adaptation enters battlefield and awaits creature type choice")
    void resolvingTriggersSubtypeChoice() {
        harness.setHand(player1, List.of(new ArcaneAdaptation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arcane Adaptation");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a creature type sets chosenSubtype on Arcane Adaptation")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new ArcaneAdaptation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        Permanent arcaneAdaptation = findPermanent(player1, "Arcane Adaptation");
        assertThat(arcaneAdaptation.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures you control gain the chosen creature type")
    void grantsChosenSubtypeToOwnCreatures() {
        Card bear = new JungleDelver();
        harness.addToBattlefield(player1, bear);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent bearPerm = findPermanent(player1, "Jungle Delver");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures retain their original types in addition to the chosen type")
    void creaturesRetainOriginalSubtypes() {
        Card elf = new JungleDelver();
        harness.addToBattlefield(player1, elf);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.WIZARD);

        Permanent elfPerm = findPermanent(player1, "Jungle Delver");

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.WIZARD);
        assertThat(gqs.hasEffectiveSubtype(gd, elfPerm, CardSubtype.MERFOLK)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, elfPerm, CardSubtype.WARRIOR)).isTrue();
    }

    @Test
    @DisplayName("Opponent's creatures do not gain the chosen type")
    void doesNotAffectOpponentCreatures() {
        Card opponentCreature = new JungleDelver();
        harness.addToBattlefield(player2, opponentCreature);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent bearPerm = findPermanent(player2, "Jungle Delver");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("No subtype granted if no creature type was chosen yet")
    void noSubtypeWithoutChoice() {
        Card bear = new JungleDelver();
        harness.addToBattlefield(player1, bear);

        // Add Arcane Adaptation without setting chosen subtype
        harness.addToBattlefield(player1, new ArcaneAdaptation());

        Permanent bearPerm = findPermanent(player1, "Jungle Delver");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Creature already of the chosen type does not get duplicate subtype")
    void noDuplicateSubtype() {
        Card goblin = new RiggingRunner();
        harness.addToBattlefield(player1, goblin);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent goblinPerm = findPermanent(player1, "Rigging Runner");

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.grantedSubtypes().stream().filter(s -> s == CardSubtype.GOBLIN).count()).isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Non-creature permanents are not affected")
    void doesNotAffectNonCreatures() {
        Card artifact = new PryingBlade();
        harness.addToBattlefield(player1, artifact);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent artifactPerm = findPermanent(player1, "Prying Blade");

        var bonus = gqs.computeStaticBonus(gd, artifactPerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creature cards in hand gain the chosen subtype (via computeGrantedSubtypesForOwnedCreatureCard)")
    void grantsChosenSubtypeToCreatureCardsInHand() {
        Card bear = new JungleDelver();
        gd.playerHands.get(player1.getId()).add(bear);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        List<CardSubtype> granted = gqs.computeGrantedSubtypesForOwnedCreatureCard(gd, player1.getId());
        assertThat(granted).contains(CardSubtype.GOBLIN);
        assertThat(gqs.cardHasSubtype(bear, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(bear, CardSubtype.MERFOLK, gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Creature cards in graveyard gain the chosen subtype")
    void grantsChosenSubtypeToCreatureCardsInGraveyard() {
        Card bear = new JungleDelver();
        gd.playerGraveyards.get(player1.getId()).add(bear);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        assertThat(gqs.cardHasSubtype(bear, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Non-creature cards in hand do not gain the chosen subtype")
    void doesNotGrantSubtypeToNonCreatureCardsInHand() {
        Card artifact = new PryingBlade();
        gd.playerHands.get(player1.getId()).add(artifact);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        assertThat(gqs.cardHasSubtype(artifact, CardSubtype.GOBLIN, gd, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Opponent's creature cards in hand do not gain the chosen subtype")
    void doesNotGrantSubtypeToOpponentCreatureCardsInHand() {
        Card bear = new JungleDelver();
        gd.playerHands.get(player2.getId()).add(bear);

        Permanent arcaneAdaptationPerm = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        arcaneAdaptationPerm.setChosenSubtype(CardSubtype.GOBLIN);

        assertThat(gqs.cardHasSubtype(bear, CardSubtype.GOBLIN, gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose creature type, creatures gain type")
    void fullIntegrationTest() {
        Card bear = new JungleDelver();
        harness.addToBattlefield(player1, bear);

        harness.setHand(player1, List.of(new ArcaneAdaptation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Cast and resolve Arcane Adaptation
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        // Choose Dinosaur
        harness.handleListChoice(player1, "DINOSAUR");

        // Verify creature gains the chosen type
        Permanent bearPerm = findPermanent(player1, "Jungle Delver");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.DINOSAUR);

        // Verify the Arcane Adaptation permanent has the chosen subtype stored
        Permanent arcaneAdaptation = findPermanent(player1, "Arcane Adaptation");
        assertThat(arcaneAdaptation.getChosenSubtype()).isEqualTo(CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Creature cards in library and exile gain the type and retain their other types")
    void grantsSubtypeInLibraryAndExile() {
        Card libraryCreature = new JungleDelver();
        Card exiledCreature = new JungleDelver();
        harness.setLibrary(player1, List.of(libraryCreature));
        harness.setExile(player1, List.of(exiledCreature));
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.DINOSAUR);

        for (Card creature : List.of(libraryCreature, exiledCreature)) {
            assertThat(gqs.getCardSubtypes(creature, gd, player1.getId()))
                    .contains(CardSubtype.DINOSAUR, CardSubtype.MERFOLK, CardSubtype.WARRIOR);
        }
    }

    @Test
    @DisplayName("Creature spells gain the chosen type before they resolve")
    void grantsSubtypeToCreatureSpell() {
        Permanent adaptation = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        adaptation.setChosenSubtype(CardSubtype.DINOSAUR);
        harness.setHand(player1, List.of(new JungleDelver()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry spell = gd.stack.getFirst();
        assertThat(gqs.getCardSubtypes(spell.getCard(), gd, spell.getControllerId()))
                .contains(CardSubtype.DINOSAUR, CardSubtype.MERFOLK, CardSubtype.WARRIOR);
        harness.passBothPriorities();
        assertThat(gqs.hasEffectiveSubtype(gd, findPermanent(player1, "Jungle Delver"), CardSubtype.DINOSAUR))
                .isTrue();
    }

    @Test
    @DisplayName("Multiple Adaptations add both types and their effects end when they leave")
    void multipleAdaptationsAndRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        Card handCreature = new JungleDelver();
        harness.setHand(player1, List.of(handCreature));
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        first.setChosenSubtype(CardSubtype.GOBLIN);
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ArcaneAdaptation());
        second.setChosenSubtype(CardSubtype.DINOSAUR);

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DINOSAUR)).isTrue();
        assertThat(gqs.getCardSubtypes(handCreature, gd, player1.getId()))
                .contains(CardSubtype.GOBLIN, CardSubtype.DINOSAUR, CardSubtype.MERFOLK);

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DINOSAUR)).isTrue();
        assertThat(gqs.getCardSubtypes(handCreature, gd, player1.getId()))
                .contains(CardSubtype.DINOSAUR, CardSubtype.MERFOLK).doesNotContain(CardSubtype.GOBLIN);

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.DINOSAUR)).isFalse();
        assertThat(gqs.getCardSubtypes(handCreature, gd, player1.getId()))
                .contains(CardSubtype.MERFOLK).doesNotContain(CardSubtype.GOBLIN, CardSubtype.DINOSAUR);
    }

    @Test
    @DisplayName("Choosing Pirate protects your creatures from Fiery Cannonade but not opposing creatures")
    void chosenTypeAffectsSubtypeFilteredDamage() {
        harness.addToBattlefield(player1, new JungleDelver());
        harness.addToBattlefield(player2, new JungleDelver());
        harness.setHand(player1, List.of(new ArcaneAdaptation(), new FieryCannonade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "PIRATE");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        harness.assertOnBattlefield(player1, "Jungle Delver");
        harness.assertNotInGraveyard(player1, "Jungle Delver");
        harness.assertNotOnBattlefield(player2, "Jungle Delver");
        harness.assertInGraveyard(player2, "Jungle Delver");
    }
}
