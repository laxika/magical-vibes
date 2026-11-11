package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Every way an ability of one trigger reaches the stack records the same trigger event: an ability whose mode
 * (CR 603.3c) or target (CR 603.3d) is chosen as it goes there carries what an ability that chooses nothing records,
 * whatever its effects are. An ability that triggers while another one's targets are being chosen is an object of its
 * own (CR 603.3b) and waits its turn instead of replacing that choice. An "if" of an effect is judged as the ability
 * resolves, so it doesn't keep the ability off the stack (CR 603.4 is the intervening "if" only).
 */
@CardUsed({GrizzlyBears.class})
class CollectorRouteRecordTest extends BaseCardTest {

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card permanent(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        card.setManaCost("{1}");
        return card;
    }

    private static ReturnCardFromGraveyardEffect returnTargetCreatureCardToHand() {
        return ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build();
    }

    private static ChooseOneAtTriggerTimeEffect gainOrDraw() {
        return new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("You gain 1 life", new GainLifeEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)))));
    }

    private static PermanentRemovalService removal() {
        return GameTestEngineContext.get().getBean(PermanentRemovalService.class);
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private Permanent attacking(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void dealCombatDamage() {
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
    }

    @Test
    @DisplayName("\"Whenever one or more creatures die, you gain 1 life\" records the death as its \"you may\" twin does")
    void oneOrMoreCreaturesDieRecordsTheDeathWhateverTheEffectIs() {
        Card plain = permanent("Plain Mourner", CardType.ENCHANTMENT);
        plain.addTrigger(Triggers.dies(Triggers.CREATURE, ObjectRelation.ANY), TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(1));
        Card optional = permanent("Willing Mourner", CardType.ENCHANTMENT);
        optional.addTrigger(Triggers.dies(Triggers.CREATURE, ObjectRelation.ANY), TriggerOptions.ONE_OR_MORE,
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        harness.addToBattlefield(player1, plain);
        harness.addToBattlefield(player1, optional);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, bears));

        StackEntry plainEntry = entriesOf("Plain Mourner").getFirst();
        StackEntry optionalEntry = entriesOf("Willing Mourner").getFirst();
        assertThat(plainEntry.getTriggeringPermanentControllerId())
                .isEqualTo(optionalEntry.getTriggeringPermanentControllerId()).isEqualTo(player2.getId());
        assertThat(plainEntry.getEventValue()).isEqualTo(optionalEntry.getEventValue()).isEqualTo(2);
        assertThat(plainEntry.getTriggeringPermanentToughnessAtTrigger())
                .isEqualTo(optionalEntry.getTriggeringPermanentToughnessAtTrigger()).isEqualTo(2);
        assertThat(plainEntry.isNonTargeting()).isEqualTo(optionalEntry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("A combat damage ability whose mode is chosen as it triggers records the damage as a plain one does")
    void combatDamageModalAbilityRecordsTheDamage() {
        Card modal = creature("Choosy Raider");
        modal.addTrigger(Triggers.dealsCombatDamageToPlayer(), gainOrDraw());
        Card plain = creature("Plain Raider");
        plain.addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(1));
        attacking(modal);
        attacking(plain);

        dealCombatDamage();
        harness.handleListChoice(player1, "You gain 1 life");

        StackEntry modalEntry = entriesOf("Choosy Raider").getFirst();
        StackEntry plainEntry = entriesOf("Plain Raider").getFirst();
        assertThat(modalEntry.getEventValue()).isEqualTo(plainEntry.getEventValue()).isEqualTo(2);
        assertThat(modalEntry.getTriggeringPlayerId()).isEqualTo(plainEntry.getTriggeringPlayerId())
                .isEqualTo(player2.getId());
        assertThat(modalEntry.getTargetId()).isEqualTo(plainEntry.getTargetId());
        assertThat(modalEntry.isNonTargeting()).isEqualTo(plainEntry.isNonTargeting());
        assertThat(modalEntry.getSourcePermanentSnapshot()).isNotNull();
        assertThat(modalEntry.getEventDamageSource()).isNotNull();
    }

    @Test
    @DisplayName("A combat damage ability with a graveyard target records the damage as a plain one does")
    void combatDamageGraveyardTargetAbilityRecordsTheDamage() {
        Card returner = creature("Grave Raider");
        returner.addTrigger(Triggers.dealsCombatDamageToPlayer(), returnTargetCreatureCardToHand());
        attacking(returner);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        StackEntry entry = entriesOf("Grave Raider").getFirst();
        assertThat(entry.getEventValue()).isEqualTo(2);
        assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
        assertThat(entry.getEventDamageSource()).isNotNull();
    }

    @Test
    @DisplayName("An Equipment's combat damage ability with a graveyard target records the damage and the creature")
    void attachedCombatDamageGraveyardTargetAbilityRecordsTheDamage() {
        Card card = permanent("Grave Blade", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), returnTargetCreatureCardToHand());
        Permanent attacker = attacking(new GrizzlyBears());
        Permanent blade = addCreatureReady(player1, card);
        blade.setAttachedTo(attacker.getId());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        StackEntry entry = entriesOf("Grave Blade").getFirst();
        assertThat(entry.getEventValue()).isEqualTo(2);
        assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(attacker.getId());
        assertThat(entry.getEventDamageSource()).isNotNull();
    }

    @Test
    @DisplayName("\"Whenever a creature you control deals combat damage to a player\" with a graveyard target names the creature that dealt it")
    void allyCombatDamageGraveyardTargetAbilityRecordsTheDealer() {
        Card card = permanent("Grave Banner", CardType.ENCHANTMENT);
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), returnTargetCreatureCardToHand());
        Permanent attacker = attacking(new GrizzlyBears());
        harness.addToBattlefield(player1, card);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        dealCombatDamage();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        StackEntry entry = entriesOf("Grave Banner").getFirst();
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(attacker.getId());
        assertThat(entry.getTriggeringPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Two \"attacks and isn't blocked, exile up to two target cards from defending player's graveyard\" abilities both reach the stack")
    void twoGraveyardChoosingUnblockedAttackAbilitiesBothReachTheStack() {
        Card first = creature("First Badger");
        first.addTrigger(Triggers.attacksUnblocked(), new ExileCardsFromGraveyardEffect(2, 0));
        Card second = creature("Second Badger");
        second.addTrigger(Triggers.attacksUnblocked(), new ExileCardsFromGraveyardEffect(2, 0));
        attacking(first);
        attacking(second);
        Card bears = new GrizzlyBears();
        Card moreBears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears, moreBears));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(moreBears.getId()));

        assertThat(entriesOf("First Badger")).hasSize(1);
        assertThat(entriesOf("Second Badger")).hasSize(1);
    }

    @Test
    @DisplayName("Two Equipment's \"whenever equipped creature attacks, return target creature card\" abilities both reach the stack")
    void twoGraveyardTargetingAttachedAttackAbilitiesBothReachTheStack() {
        Card first = permanent("First Lantern", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        first.addTrigger(Triggers.attacks(), returnTargetCreatureCardToHand());
        Card second = permanent("Second Lantern", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        second.addTrigger(Triggers.attacks(), returnTargetCreatureCardToHand());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, first).setAttachedTo(attacker.getId());
        addCreatureReady(player1, second).setAttachedTo(attacker.getId());
        Card bears = new GrizzlyBears();
        Card moreBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, moreBears));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(moreBears.getId()));

        assertThat(entriesOf("First Lantern")).hasSize(1);
        assertThat(entriesOf("Second Lantern")).hasSize(1);
    }

    @Test
    @DisplayName("\"When this dies, choose one\" records the death as a plain dies ability does")
    void ownDeathModalAbilityRecordsTheDeath() {
        Card modal = creature("Choosy Martyr");
        modal.addTrigger(Triggers.dies(ObjectRelation.THIS), gainOrDraw());
        Card plain = creature("Plain Martyr");
        plain.addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(1));
        Permanent modalPermanent = addCreatureReady(player1, modal);
        Permanent plainPermanent = addCreatureReady(player1, plain);

        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, plainPermanent));
        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, modalPermanent));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, "You gain 1 life");

        StackEntry modalEntry = entriesOf("Choosy Martyr").getFirst();
        StackEntry plainEntry = entriesOf("Plain Martyr").getFirst();
        assertThat(modalEntry.getEventValue()).isEqualTo(plainEntry.getEventValue()).isEqualTo(2);
        assertThat(modalEntry.getTriggeringCardId()).isEqualTo(modal.getId());
        assertThat(plainEntry.getTriggeringCardId()).isEqualTo(plain.getId());
        assertThat(modalEntry.getSourcePermanentSnapshot()).isNotNull();
        assertThat(modalEntry.getTriggeringPermanentPowerAtTrigger())
                .isEqualTo(plainEntry.getTriggeringPermanentPowerAtTrigger());
    }

    @Test
    @DisplayName("\"Whenever another creature you control leaves the battlefield\" goes on the stack with an effect whose \"if\" is false as it triggers")
    void allyCreatureLeavesAbilityKeepsAnEffectWhoseIfIsFalseAsItTriggers() {
        Card card = creature("Patient Watcher");
        card.addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new ConditionalEffect(new ControllerLifeAtLeast(30, false), new DrawCardEffect(1)));
        addCreatureReady(player1, card);
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> removal().removePermanentToHand(gd, leaving));

        assertThat(entriesOf("Patient Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
        // The "if" is true by the time the ability resolves.
        harness.setLife(player1, 30);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("A combat damage ability whose two target groups are chosen one by one records the damage as a plain one does")
    void combatDamageAbilityWithTwoTargetGroupsRecordsTheDamage() {
        Card card = creature("Twin Wrecker");
        CardEffect destroy = new DestroyTargetPermanentEffect();
        CardEffect boost = new BoostTargetCreatureEffect(1, 1);
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), destroy, boost);
        card.target(TargetFilters.creature()).bindEffect(destroy);
        card.target(TargetFilters.creature()).bindEffect(boost);
        Card plain = creature("Plain Wrecker");
        plain.addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(1));
        Permanent wrecker = attacking(card);
        Permanent plainWrecker = attacking(plain);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        dealCombatDamage();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, plainWrecker.getId());

        StackEntry entry = entriesOf("Twin Wrecker").getFirst();
        StackEntry plainEntry = entriesOf("Plain Wrecker").getFirst();
        assertThat(entry.getEventValue()).isEqualTo(plainEntry.getEventValue()).isEqualTo(2);
        assertThat(entry.getTriggeringPlayerId()).isEqualTo(plainEntry.getTriggeringPlayerId())
                .isEqualTo(player2.getId());
        assertThat(entry.getEventDamageSource()).isNotNull();
        assertThat(entry.getEventDamageSource().getId()).isEqualTo(wrecker.getId());
        assertThat(entry.getTargetIds()).containsExactly(bears.getId(), plainWrecker.getId());
    }

    @Test
    @DisplayName("\"When this dies, choose one\" with a targeted mode records the death as a plain dies ability does")
    void ownDeathModalAbilityWithTargetedModeRecordsTheDeath() {
        Card modal = creature("Vengeful Martyr");
        modal.addTrigger(Triggers.dies(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(
                List.of(new ChooseOneEffect.ChooseOneOption("Destroy target creature",
                                new DestroyTargetPermanentEffect(), TargetFilters.creature()),
                        new ChooseOneEffect.ChooseOneOption("You gain 1 life", new GainLifeEffect(1))))));
        Card plain = creature("Quiet Martyr");
        plain.addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(1));
        Permanent modalPermanent = addCreatureReady(player1, modal);
        Permanent plainPermanent = addCreatureReady(player1, plain);
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, plainPermanent));
        harness.inMutationScope(() -> removal().tryDestroyPermanent(gd, modalPermanent));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextTriggeredModalTrigger(gd));
        harness.handleListChoice(player1, "Destroy target creature");
        harness.handlePermanentChosen(player1, victim.getId());

        StackEntry modalEntry = entriesOf("Vengeful Martyr").getFirst();
        StackEntry plainEntry = entriesOf("Quiet Martyr").getFirst();
        assertThat(modalEntry.getTargetId()).isEqualTo(victim.getId());
        assertThat(modalEntry.getEventValue()).isEqualTo(plainEntry.getEventValue()).isEqualTo(2);
        assertThat(modalEntry.getTriggeringCardId()).isEqualTo(modal.getId());
        assertThat(modalEntry.getSourcePermanentSnapshot()).isNotNull();
        assertThat(modalEntry.getTriggeringPermanentPowerAtTrigger())
                .isEqualTo(plainEntry.getTriggeringPermanentPowerAtTrigger());
    }

    @Test
    @DisplayName("A second \"whenever this attacks, return target creature card from your graveyard\" ability chooses from your graveyard")
    void secondSelfAttackGraveyardAbilityChoosesFromItsOwnGraveyard() {
        Card first = creature("First Digger");
        first.addTrigger(Triggers.attacks(), returnTargetCreatureCardToHand());
        Card second = creature("Second Digger");
        second.addTrigger(Triggers.attacks(), returnTargetCreatureCardToHand());
        addCreatureReady(player1, first);
        addCreatureReady(player1, second);
        Card bears = new GrizzlyBears();
        Card moreBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears, moreBears));
        harness.setGraveyard(player2, List.of());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0, 1)));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(moreBears.getId()));

        assertThat(entriesOf("First Digger")).hasSize(1);
        assertThat(entriesOf("Second Digger")).hasSize(1);
    }
}
