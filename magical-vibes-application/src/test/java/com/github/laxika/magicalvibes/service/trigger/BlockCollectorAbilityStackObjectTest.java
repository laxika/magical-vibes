package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code CombatBlockService}'s block-step collectors put each triggered ability on the stack as one object of all its
 * effects (CR 603.3): a "you may" among an ability's effects stays in it and is asked as the ability resolves
 * (CR 603.5), a target is chosen once for the whole ability (CR 603.3d), and two abilities of one permanent or card on
 * one trigger are two objects (CR 603.3b), whoever gave them to it. An ability that triggers once each combat is told
 * from one that triggers for each creature by the ability itself (CR 509.3a, 509.3b).
 */
@CardUsed({GrizzlyBears.class})
class BlockCollectorAbilityStackObjectTest extends BaseCardTest {

    private static Card watcher(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card attachment(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        return card;
    }

    private static TriggeredAbility drawAndGain(Trigger trigger) {
        return new TriggeredAbility(trigger, TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new DrawCardEffect(1)), new EffectRegistration(new GainLifeEffect(1))));
    }

    private static ReturnCardFromGraveyardEffect returnTargetCreatureCardToHand() {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private List<Integer> effectCountsOf(String name) {
        return entriesOf(name).stream().map(entry -> entry.getEffectsToResolve().size()).toList();
    }

    private Permanent attackingBears() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent attacking(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlockers(BlockerAssignment... assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(assignments));
    }

    @Test
    @DisplayName("Two \"whenever this blocks\" abilities are two stack objects")
    void twoBlockAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Guard");
        card.addTrigger(Triggers.blocks(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.blocks(), new GainLifeEffect(2));
        attackingBears();
        addCreatureReady(player2, card);

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Guard")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("A block ability of two effects a creature has until end of turn stays apart from its printed one")
    void temporaryBlockAbilityStaysApartFromThePrintedOne() {
        Card card = watcher("Drilled Guard");
        card.addTrigger(Triggers.blocks(), new GainLifeEffect(2));
        attackingBears();
        Permanent guard = addCreatureReady(player2, card);
        guard.addTemporaryAbility(drawAndGain(Triggers.blocks()));

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Drilled Guard")).containsExactly(1, 2);
    }

    @Test
    @DisplayName("\"Blocks a creature\" beside \"blocks\" triggers for each creature blocked, \"blocks\" once")
    void blocksACreatureTriggersForEachBesideAnOnceEachCombatAbility() {
        Card card = watcher("Wide Guard");
        card.addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        card.addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new GainLifeEffect(1), new GainLifeEffect(2));
        attackingBears();
        attackingBears();
        Permanent guard = addCreatureReady(player2, card);
        guard.addTemporaryAbility(new TriggeredAbility(Triggers.blocks(), new DrawCardEffect(1)));

        declareBlockers(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1));

        assertThat(effectCountsOf("Wide Guard")).containsExactlyInAnyOrder(2, 1, 1);
    }

    @Test
    @DisplayName("\"Blocks\" once each combat a creature has until end of turn triggers once for two creatures blocked")
    void temporaryOnceEachCombatBlockAbilityTriggersOnce() {
        Card card = watcher("Borrowed Guard");
        card.addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        attackingBears();
        attackingBears();
        Permanent guard = addCreatureReady(player2, card);
        guard.addTemporaryAbility(new TriggeredAbility(Triggers.blocks(), TriggerOptions.ONE_OR_MORE,
                new DrawCardEffect(1)));

        declareBlockers(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1));

        assertThat(entriesOf("Borrowed Guard")).hasSize(1);
    }

    @Test
    @DisplayName("\"Blocks\" once each combat doesn't trigger again when an effect makes a blocking creature block another")
    void onceEachCombatBlockAbilityTriggersOnceForBlocksMadeByAnEffect() {
        Card card = watcher("Late Guard");
        card.addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        card.addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new GainLifeEffect(1));
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent guard = addCreatureReady(player2, card);
        declareAttackersAndPrepareBlockers(List.of(0, 1));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of()));
        gd.stack.add(TestStackDoor.triggered(card, player2.getId(), "Late Guard's ability",
                new ArrayList<>(List.of(new MakeAllUnblockedCreaturesAttackingControllerBlockedBySourceEffect())),
                null, guard.getId()));

        harness.passBothPriorities();

        assertThat(guard.getBlockingTargetIds()).hasSize(2);
        assertThat(entriesOf("Late Guard")).hasSize(1);
    }

    @Test
    @DisplayName("Two \"whenever this blocks two or more creatures\" abilities are two stack objects")
    void twoBlocksMultipleAbilitiesAreTwoStackObjects() {
        Card card = watcher("Broad Guard");
        card.addStaticEffect(new CanBlockAnyNumberOfCreaturesEffect());
        card.addTrigger(Triggers.blocksTwoOrMore(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.blocksTwoOrMore(), new GainLifeEffect(2));
        attackingBears();
        attackingBears();
        addCreatureReady(player2, card);

        declareBlockers(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1));

        assertThat(effectCountsOf("Broad Guard")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever this becomes blocked\" abilities are two stack objects")
    void twoBecomesBlockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Raider");
        card.addTrigger(Triggers.becomesBlocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.becomesBlocked(), new GainLifeEffect(2));
        attacking(card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Raider")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("A \"becomes blocked\" ability of two effects a static ability grants is one stack object")
    void grantedBecomesBlockedAbilityIsOneStackObject() {
        Card card = watcher("Drilled Raider");
        card.addStaticEffect(new GrantTriggeredAbilityEffect(drawAndGain(Triggers.becomesBlocked()),
                GrantScope.ALL_OWN_CREATURES));
        attacking(card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(entriesOf("Drilled Raider")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("A \"becomes blocked\" ability with a graveyard target and another one are two stack objects")
    void becomesBlockedAbilityWithGraveyardTargetStaysApartFromAnotherAbility() {
        Card card = watcher("Grave Raider");
        card.addTrigger(Triggers.becomesBlocked(), returnTargetCreatureCardToHand());
        card.addTrigger(Triggers.becomesBlocked(), new GainLifeEffect(1));
        attacking(card);
        addCreatureReady(player2, new GrizzlyBears());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        declareBlockers(new BlockerAssignment(0, 0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(effectCountsOf("Grave Raider")).containsExactlyInAnyOrder(1, 1);
    }

    @Test
    @DisplayName("Two \"whenever this becomes blocked by a creature\" abilities are two stack objects for each blocker")
    void twoBecomesBlockedByACreatureAbilitiesAreTwoStackObjects() {
        Card card = watcher("Spiked Raider");
        card.addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new DrawCardEffect(1),
                new GainLifeEffect(1));
        card.addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new GainLifeEffect(2));
        attacking(card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Spiked Raider")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"becomes blocked by a creature\" abilities of an Equipment are two stack objects")
    void twoAttachedBecomesBlockedByACreatureAbilitiesAreTwoStackObjects() {
        Card card = attachment("Spiked Lens", CardType.ARTIFACT);
        card.addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new DrawCardEffect(1),
                new GainLifeEffect(1));
        card.addTrigger(Triggers.becomesBlocked(), TriggerOptions.EACH_BLOCKER, new GainLifeEffect(2));
        Permanent attacker = attackingBears();
        Permanent lens = addCreatureReady(player1, card);
        lens.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Spiked Lens")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever this attacks and isn't blocked\" abilities are two stack objects")
    void twoAttacksUnblockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Prowler");
        card.addTrigger(Triggers.attacksUnblocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.attacksUnblocked(), new GainLifeEffect(2));
        attacking(card);

        declareBlockers();

        assertThat(effectCountsOf("Twin Prowler")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("An \"attacks and isn't blocked\" ability of two effects a static ability grants is one stack object")
    void grantedAttacksUnblockedAbilityIsOneStackObject() {
        Card card = watcher("Drilled Prowler");
        card.addStaticEffect(new GrantTriggeredAbilityEffect(drawAndGain(Triggers.attacksUnblocked()),
                GrantScope.ALL_OWN_CREATURES));
        attacking(card);

        declareBlockers();

        assertThat(entriesOf("Drilled Prowler")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Attacks and isn't blocked, you may destroy target creature and you gain 1 life\" is one stack object")
    void attacksUnblockedAbilityWithTargetedMayIsOneStackObject() {
        Card card = watcher("Generous Prowler");
        card.addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy it?"), new GainLifeEffect(1));
        attacking(card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareBlockers();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(entriesOf("Generous Prowler")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"whenever enchanted creature attacks and isn't blocked\" abilities are two stack objects")
    void twoEnchantedCreatureAttacksUnblockedAbilitiesAreTwoStackObjects() {
        Card card = attachment("Twin Mantle", CardType.ENCHANTMENT);
        card.addTrigger(Triggers.enchantedCreatureAttacksUnblocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.enchantedCreatureAttacksUnblocked(), new GainLifeEffect(2));
        Permanent attacker = attackingBears();
        Permanent mantle = addCreatureReady(player1, card);
        mantle.setAttachedTo(attacker.getId());

        declareBlockers();

        assertThat(effectCountsOf("Twin Mantle")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever a creature you control attacks and isn't blocked\" abilities are two stack objects")
    void twoAllyAttacksUnblockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Bandit");
        card.addTrigger(Triggers.creatureYouControlAttacksUnblocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureYouControlAttacksUnblocked(), new GainLifeEffect(2));
        addCreatureReady(player1, card);
        attackingBears();

        declareBlockers();

        assertThat(effectCountsOf("Twin Bandit")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"a creature you control attacks and isn't blocked\" abilities of a card in exile are two stack objects")
    void twoExileAllyAttacksUnblockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Exiled Bandit");
        card.addTrigger(new TriggerCondition(Triggers.creatureYouControlAttacksUnblocked(), AbilityZones.EXILE),
                TriggerOptions.DEFAULT, new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(new TriggerCondition(Triggers.creatureYouControlAttacksUnblocked(), AbilityZones.EXILE),
                TriggerOptions.DEFAULT, new GainLifeEffect(2));
        gd.exiledCards.add(new ExiledCardEntry(card, player1.getId(), null));
        attackingBears();

        declareBlockers();

        assertThat(effectCountsOf("Exiled Bandit")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"one or more creatures attack you and aren't blocked\" abilities are two stack objects")
    void twoCreaturesAttackYouUnblockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Jewel");
        card.addTrigger(Triggers.creaturesAttackYouUnblocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creaturesAttackYouUnblocked(), new GainLifeEffect(2));
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, card);
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> gs.declareBlockers(gd, player2, List.of()));

        assertThat(effectCountsOf("Twin Jewel")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever a creature you control becomes blocked\" abilities are two stack objects")
    void twoAllyBecomesBlockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Alpha");
        card.addTrigger(Triggers.creatureYouControlBecomesBlocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureYouControlBecomesBlocked(), new GainLifeEffect(2));
        attackingBears();
        addCreatureReady(player1, card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Alpha")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"one or more creatures you control become blocked\" abilities are two stack objects")
    void twoAllyCreaturesBecomeBlockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Spender");
        card.addTrigger(Triggers.creaturesYouControlBecomeBlocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creaturesYouControlBecomeBlocked(), new GainLifeEffect(2));
        attackingBears();
        addCreatureReady(player1, card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Spender")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever a creature becomes blocked\" abilities are two stack objects")
    void twoAnyCreatureBecomesBlockedAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Sliver");
        card.addTrigger(Triggers.creatureBecomesBlocked(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureBecomesBlocked(), new GainLifeEffect(2));
        attackingBears();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, card);

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Sliver")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever a creature blocks\" abilities are two stack objects")
    void twoAnyCreatureBlocksAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Gladiator");
        card.addTrigger(Triggers.creatureBlocks(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureBlocks(), new GainLifeEffect(2));
        attackingBears();
        addCreatureReady(player1, card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Gladiator")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever one or more creatures block\" abilities are two stack objects")
    void twoAnyCreaturesBlockAbilitiesAreTwoStackObjects() {
        Card card = watcher("Twin Tide");
        card.addTrigger(Triggers.creaturesBlock(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creaturesBlock(), new GainLifeEffect(2));
        attackingBears();
        addCreatureReady(player1, card);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlockers(new BlockerAssignment(0, 0));

        assertThat(effectCountsOf("Twin Tide")).containsExactly(2, 1);
    }
}
