package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SpinedThopter;
import com.github.laxika.magicalvibes.cards.s.ShrineOfBurningRage;
import com.github.laxika.magicalvibes.cards.v.VaultSkirge;
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

@CardUsed({Xenograft.class, SpinedThopter.class, VaultSkirge.class, ShrineOfBurningRage.class, Opalescence.class})
class XenograftTest extends BaseCardTest {

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
        assertThat(entry.getCard()).isInstanceOf(Xenograft.class);
    }

    @Test
    @DisplayName("Resolving Xenograft enters battlefield and awaits creature type choice")
    void resolvingTriggersSubtypeChoice() {
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Xenograft");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId()).isEqualTo(player1.getId());
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

        Permanent xenograft = findPermanent(player1, "Xenograft");
        assertThat(xenograft.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures you control gain the chosen creature type")
    void grantsChosenSubtypeToOwnCreatures() {
        Card bear = new SpinedThopter();
        harness.addToBattlefield(player1, bear);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = harness.addToBattlefieldAndReturn(player1, xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent bearPerm = findPermanent(player1, "Spined Thopter");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Creatures retain their original types in addition to the chosen type")
    void creaturesRetainOriginalSubtypes() {
        Card elf = new SpinedThopter();
        harness.addToBattlefield(player1, elf);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = harness.addToBattlefieldAndReturn(player1, xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.WIZARD);

        Permanent elfPerm = findPermanent(player1, "Spined Thopter");

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.WIZARD);
        assertThat(gqs.effectiveCreatureSubtypes(gd, elfPerm))
                .contains(CardSubtype.PHYREXIAN, CardSubtype.THOPTER, CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Opponent's creatures do not gain the chosen type")
    void doesNotAffectOpponentCreatures() {
        Card opponentCreature = new SpinedThopter();
        harness.addToBattlefield(player2, opponentCreature);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = harness.addToBattlefieldAndReturn(player1, xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent bearPerm = findPermanent(player2, "Spined Thopter");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("No subtype granted if no creature type was chosen yet")
    void noSubtypeWithoutChoice() {
        Card bear = new SpinedThopter();
        harness.addToBattlefield(player1, bear);

        // Add Xenograft without setting chosen subtype
        harness.addToBattlefield(player1, new Xenograft());

        Permanent bearPerm = findPermanent(player1, "Spined Thopter");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Creature already of the chosen type does not get duplicate subtype")
    void noDuplicateSubtype() {
        Card thopter = new SpinedThopter();
        harness.addToBattlefield(player1, thopter);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = harness.addToBattlefieldAndReturn(player1, xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.THOPTER);

        Permanent thopterPerm = findPermanent(player1, "Spined Thopter");

        var bonus = gqs.computeStaticBonus(gd, thopterPerm);
        assertThat(bonus.grantedSubtypes().stream().filter(s -> s == CardSubtype.THOPTER).count()).isLessThanOrEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, thopterPerm)).contains(CardSubtype.THOPTER);
    }

    @Test
    @DisplayName("Non-creature permanents are not affected")
    void doesNotAffectNonCreatures() {
        Card artifact = new ShrineOfBurningRage();
        harness.addToBattlefield(player1, artifact);

        Xenograft xenograftCard = new Xenograft();
        Permanent xenograftPerm = harness.addToBattlefieldAndReturn(player1, xenograftCard);
        xenograftPerm.setChosenSubtype(CardSubtype.GOBLIN);

        Permanent artifactPerm = findPermanent(player1, "Shrine of Burning Rage");

        var bonus = gqs.computeStaticBonus(gd, artifactPerm);
        assertThat(bonus.grantedSubtypes()).doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Full flow: cast, resolve, choose creature type, creatures gain type")
    void fullIntegrationTest() {
        Card bear = new SpinedThopter();
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
        Permanent bearPerm = findPermanent(player1, "Spined Thopter");

        var bonus = gqs.computeStaticBonus(gd, bearPerm);
        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);

        // Verify the Xenograft permanent has the chosen subtype stored
        Permanent xenograft = findPermanent(player1, "Xenograft");
        assertThat(xenograft.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("A creature entering later gains the chosen type while retaining its original types")
    void affectsCreaturesEnteringLater() {
        castXenograftChoosing("GOBLIN");
        harness.setHand(player1, List.of(new VaultSkirge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(gqs.getCardSubtypes(gd.stack.getFirst().getCard(), gd, player1.getId()))
                .doesNotContain(CardSubtype.GOBLIN);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, findPermanent(player1, "Vault Skirge")))
                .contains(CardSubtype.PHYREXIAN, CardSubtype.IMP, CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Xenograft does not affect creature cards in hand, library, graveyard, or exile")
    void doesNotAffectCardsOutsideBattlefield() {
        castXenograftChoosing("GOBLIN");
        Card handCard = new SpinedThopter();
        Card libraryCard = new SpinedThopter();
        Card graveyardCard = new SpinedThopter();
        Card exiledCard = new SpinedThopter();
        harness.setHand(player1, List.of(handCard));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setExile(player1, List.of(exiledCard));

        for (Card card : List.of(handCard, libraryCard, graveyardCard, exiledCard)) {
            assertThat(gqs.getCardSubtypes(card, gd, player1.getId())).doesNotContain(CardSubtype.GOBLIN);
        }
    }

    @Test
    @DisplayName("Multiple Xenografts add their chosen types independently")
    void multipleXenograftsAddTypes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinedThopter());
        castXenograftChoosing("GOBLIN");
        castXenograftChoosing("WIZARD");

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.PHYREXIAN, CardSubtype.THOPTER, CardSubtype.GOBLIN, CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Removing Xenograft removes only the type it granted")
    void typeGrantEndsWhenXenograftLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SpinedThopter());
        castXenograftChoosing("GOBLIN");
        Permanent xenograft = findPermanent(player1, "Xenograft");
        assertThat(gqs.hasEffectiveSubtype(gd, creature, CardSubtype.GOBLIN)).isTrue();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, xenograft);

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.PHYREXIAN, CardSubtype.THOPTER)
                .doesNotContain(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("An animated Xenograft is itself a creature of the chosen type")
    void animatedXenograftIncludesItself() {
        harness.addToBattlefield(player1, new Opalescence());
        castXenograftChoosing("GOBLIN");
        Permanent xenograft = findPermanent(player1, "Xenograft");

        assertThat(gqs.isCreature(gd, xenograft)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, xenograft, CardSubtype.GOBLIN)).isTrue();
    }

    private void castXenograftChoosing(String subtype) {
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }
}
