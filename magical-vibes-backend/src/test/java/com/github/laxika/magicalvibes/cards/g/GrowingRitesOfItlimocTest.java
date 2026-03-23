package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.ItlimocCradleOfTheSun;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsPermanentCountConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrowingRitesOfItlimocTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Front face has ETB look-at-top-4 effect for creatures")
    void frontFaceHasCorrectETBEffect() {
        GrowingRitesOfItlimoc card = new GrowingRitesOfItlimoc();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        var effect = (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect)
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(4);
        assertThat(effect.predicate()).isEqualTo(new CardTypePredicate(CardType.CREATURE));
        assertThat(effect.anyNumber()).isFalse();
    }

    @Test
    @DisplayName("Front face has end step transform trigger for 4+ creatures")
    void frontFaceHasCorrectEndStepTrigger() {
        GrowingRitesOfItlimoc card = new GrowingRitesOfItlimoc();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(ControlsPermanentCountConditionalEffect.class);
        var conditional = (ControlsPermanentCountConditionalEffect)
                card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(conditional.minCount()).isEqualTo(4);
        assertThat(conditional.filter()).isInstanceOf(PermanentIsCreaturePredicate.class);
        assertThat(conditional.wrapped()).isInstanceOf(TransformSelfEffect.class);
    }

    @Test
    @DisplayName("Front face has back face linked")
    void frontFaceHasBackFace() {
        GrowingRitesOfItlimoc card = new GrowingRitesOfItlimoc();

        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard()).isInstanceOf(ItlimocCradleOfTheSun.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("ItlimocCradleOfTheSun");
    }

    @Test
    @DisplayName("Back face has two activated mana abilities")
    void backFaceHasCorrectAbilities() {
        GrowingRitesOfItlimoc card = new GrowingRitesOfItlimoc();
        ItlimocCradleOfTheSun backFace = (ItlimocCradleOfTheSun) card.getBackFaceCard();

        assertThat(backFace.getActivatedAbilities()).hasSize(2);

        // {T}: Add {G}.
        var tapForGreen = backFace.getActivatedAbilities().get(0);
        assertThat(tapForGreen.isRequiresTap()).isTrue();
        assertThat(tapForGreen.getManaCost()).isNull();
        assertThat(tapForGreen.getEffects()).hasSize(1);
        assertThat(tapForGreen.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);
        assertThat(((AwardManaEffect) tapForGreen.getEffects().getFirst()).color()).isEqualTo(ManaColor.GREEN);

        // {T}: Add {G} for each creature you control.
        var tapForCreatures = backFace.getActivatedAbilities().get(1);
        assertThat(tapForCreatures.isRequiresTap()).isTrue();
        assertThat(tapForCreatures.getManaCost()).isNull();
        assertThat(tapForCreatures.getEffects()).hasSize(1);
        assertThat(tapForCreatures.getEffects().getFirst()).isInstanceOf(AddManaPerControlledPermanentEffect.class);
        var manaPerCreature = (AddManaPerControlledPermanentEffect) tapForCreatures.getEffects().getFirst();
        assertThat(manaPerCreature.color()).isEqualTo(ManaColor.GREEN);
        assertThat(manaPerCreature.predicate()).isInstanceOf(PermanentIsCreaturePredicate.class);
    }

    // ===== ETB: look at top 4, may reveal creature =====

    @Test
    @DisplayName("ETB offers creature cards among top 4 for selection")
    void etbOffersCreatureCards() {
        setupTopCards(List.of(
                createCreature("Bear", 2, 2),
                createNonCreature("Bolt"),
                createCreature("Elf", 1, 1),
                createNonCreature("Enchant")
        ));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment, triggers ETB

        // ETB is pushed onto the stack as a triggered ability
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.interaction.librarySearch().playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.librarySearch().canFailToFind()).isTrue();
        assertThat(gd.interaction.librarySearch().cards()).hasSize(2);
        assertThat(gd.interaction.librarySearch().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Bear", "Elf");
    }

    @Test
    @DisplayName("ETB allows choosing a creature to put in hand")
    void etbChooseCreatureToHand() {
        Card bear = createCreature("Bear", 2, 2);
        Card bolt = createNonCreature("Bolt");
        Card elf = createCreature("Elf", 1, 1);
        Card enchant = createNonCreature("Enchant");
        setupTopCards(List.of(bear, bolt, elf, enchant));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve ETB

        // Choose the first creature (Bear)
        gs.handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Bear"));
        // Remaining 3 cards should be offered for reorder
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(3);
    }

    @Test
    @DisplayName("ETB allows declining to choose a creature")
    void etbDeclineCreature() {
        setupTopCards(List.of(
                createCreature("Bear", 2, 2),
                createNonCreature("Bolt"),
                createCreature("Elf", 1, 1),
                createNonCreature("Enchant")
        ));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve ETB

        // Decline to choose (-1 = fail to find)
        gs.handleLibraryCardChosen(gd, player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        // All 4 cards should be offered for reorder
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(4);
    }

    @Test
    @DisplayName("ETB with no creatures among top 4 skips to reorder")
    void etbNoCreaturesSkipsToReorder() {
        setupTopCards(List.of(
                createNonCreature("Bolt"),
                createNonCreature("Enchant"),
                createNonCreature("Artifact"),
                createNonCreature("Sorcery")
        ));
        harness.setHand(player1, List.of(new GrowingRitesOfItlimoc()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment
        harness.passBothPriorities(); // resolve ETB

        // No creatures → directly go to reorder
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_REORDER);
        assertThat(gd.interaction.libraryView().reorderCards()).hasSize(4);
    }

    // ===== End step transform =====

    @Test
    @DisplayName("Transforms at end step with exactly 4 creatures")
    void transformsWithFourCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step, trigger goes on stack
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.getCard().getName()).isEqualTo("Itlimoc, Cradle of the Sun");
    }

    @Test
    @DisplayName("Transforms at end step with more than 4 creatures")
    void transformsWithFiveCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step
        harness.passBothPriorities(); // resolve transform trigger

        assertThat(enchantment.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not transform at end step with only 3 creatures")
    void doesNotTransformWithThreeCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to end step — no trigger

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not transform at end step with zero creatures")
    void doesNotTransformWithZeroCreatures() {
        Permanent enchantment = addEnchantmentReady(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step")
    void doesNotTriggerOnOpponentEndStep() {
        Permanent enchantment = addEnchantmentReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        // It's player2's turn, not player1's
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
    }

    // ===== Back face: Itlimoc mana abilities =====

    @Test
    @DisplayName("Itlimoc basic tap adds one green mana")
    void itlimocBasicTapAddsGreen() {
        Permanent itlimoc = addTransformedItlimoc(player1);

        int itlimocIdx = indexOf(player1, itlimoc);
        harness.activateAbility(player1, itlimocIdx, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Itlimoc per-creature tap adds green for each creature")
    void itlimocPerCreatureTapAddsGreenPerCreature() {
        Permanent itlimoc = addTransformedItlimoc(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);
        addCreatureReady(player1);

        int itlimocIdx = indexOf(player1, itlimoc);
        harness.activateAbility(player1, itlimocIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Itlimoc per-creature tap with zero creatures adds zero mana")
    void itlimocPerCreatureTapWithZeroCreatures() {
        Permanent itlimoc = addTransformedItlimoc(player1);

        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
        int itlimocIdx = indexOf(player1, itlimoc);
        harness.activateAbility(player1, itlimocIdx, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore);
    }

    // ===== Helpers =====

    private Permanent addEnchantmentReady(Player player) {
        GrowingRitesOfItlimoc card = new GrowingRitesOfItlimoc();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTransformedItlimoc(Player player) {
        GrowingRitesOfItlimoc card = new GrowingRitesOfItlimoc();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player) {
        Card creature = createCreature("Test Creature", 2, 2);
        Permanent perm = new Permanent(creature);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private Card createNonCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{R}");
        card.setColor(CardColor.RED);
        return card;
    }

    private void setupTopCards(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
