package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.CribSwap;
import com.github.laxika.magicalvibes.cards.k.KithkinBrinefarer;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SignatureSpells.class, Cancel.class, CounselOfTheSoratami.class, Shock.class,
        CribSwap.class, KithkinBrinefarer.class})
class SignatureSpellsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by seeking two exact-mana-value-three spells into source-tracked exile")
    void seeksTwoEligibleSpellsIntoExile() {
        Cancel cancel = new Cancel();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(cancel, counsel, shock));
        harness.setHand(player1, List.of(new SignatureSpells()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Signature Spells");
        assertThat(gd.getCardsExiledByPermanent(source.getId()))
                .containsExactlyInAnyOrder(cancel, counsel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
    }

    @Test
    @DisplayName("Upkeep can copy and cast one spell exiled with Signature Spells for free")
    void upkeepCopiesAndCastsExiledSpell() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.addToBattlefield(player1, new SignatureSpells());
        Permanent source = findPermanent(player1, "Signature Spells");
        gd.exiledCards.add(new ExiledCardEntry(counsel, player1.getId(), source.getId()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Counsel of the Soratami")
                && entry.isCopy());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(counsel);
    }

    @Test
    void seeksTheOnlyEligibleCardWithoutMovingOtherCards() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock first = new Shock();
        Shock second = new Shock();
        KithkinBrinefarer creature = new KithkinBrinefarer();
        SignatureSpells enchantment = new SignatureSpells();
        harness.setLibrary(player1, List.of(first, counsel, creature, enchantment, second));
        harness.setHand(player1, List.of(new SignatureSpells()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Signature Spells");
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(counsel);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, creature, enchantment, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineToCopyAtUpkeep() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.addToBattlefield(player1, new SignatureSpells());
        Permanent source = findPermanent(player1, "Signature Spells");
        gd.exiledCards.add(new ExiledCardEntry(counsel, player1.getId(), source.getId()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(counsel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosesOneOfTwoLinkedCardsWithoutUsingAnotherSourcesCard() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Cancel cancel = new Cancel();
        CounselOfTheSoratami unrelated = new CounselOfTheSoratami();
        harness.addToBattlefield(player1, new SignatureSpells());
        Permanent source = findPermanent(player1, "Signature Spells");
        gd.exiledCards.add(new ExiledCardEntry(counsel, player1.getId(), source.getId()));
        gd.exiledCards.add(new ExiledCardEntry(cancel, player1.getId(), source.getId()));
        gd.exiledCards.add(new ExiledCardEntry(unrelated, player1.getId(), java.util.UUID.randomUUID()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.ExiledSpellCopyChoice choice =
                (PendingInteraction.ExiledSpellCopyChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(counsel.getId(), cancel.getId());
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isTrue();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Counsel of the Soratami");
        resolveAllTriggers();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactlyInAnyOrder(counsel, cancel);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SignatureSpells());
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayCopyWithoutCasting() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.addToBattlefield(player1, new SignatureSpells());
        Permanent source = findPermanent(player1, "Signature Spells");
        gd.exiledCards.add(new ExiledCardEntry(counsel, player1.getId(), source.getId()));

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(counsel);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryLeavesNoCardToCopy() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SignatureSpells()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Signature Spells");
        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        advanceToUpkeep(player1);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    void seekingBeforeExilingTriggersCardsPutIntoHandFromLibraryAbilities() {
        CribSwap sought = new CribSwap();
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(new SignatureSpells()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Signature Spells");
        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(sought);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(card -> card.getName().equals("Crib Swap") && !card.getId().equals(sought.getId()));
    }
}
