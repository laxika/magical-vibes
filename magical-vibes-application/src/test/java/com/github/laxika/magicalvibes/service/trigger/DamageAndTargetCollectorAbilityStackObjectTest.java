package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.service.trigger.pipeline.DamageTriggers;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code TriggerCollectionService}'s damage and becomes-the-target collectors put each triggered ability on the stack
 * as one object of all its effects (CR 603.3): an ability of two effects is one object an opponent can't respond
 * between, and two abilities of one permanent on one trigger are two objects (CR 603.3b), never joined into one.
 */
@CardUsed({GrizzlyBears.class, Shock.class})
class DamageAndTargetCollectorAbilityStackObjectTest extends BaseCardTest {

    private static Card watcher(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private TriggerCollectionService collectors() {
        return harness.getTriggerCollectionService();
    }

    private DamageTriggers damageCollectors() {
        return harness.getDamageTriggers();
    }

    /** An opponent's Shock on the stack with {@code targetIds} as its targets. */
    private StackEntry opponentSpellTargeting(UUID... targetIds) {
        StackEntry spell = new StackEntry(StackEntryType.INSTANT_SPELL, new Shock(), player2.getId(), "Shock",
                List.of(), (UUID) null, List.of(targetIds));
        gd.stack.add(spell);
        return spell;
    }

    @Test
    @DisplayName("\"Whenever this deals damage to a player, draw a card and gain 1 life\" is one stack object")
    void damageToPlayerAbilityIsOneStackObject() {
        Card card = watcher("Damage Dealer");
        card.addTrigger(Triggers.dealsDamageToPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        Permanent dealer = addCreatureReady(player1, card);

        harness.inMutationScope(() -> damageCollectors().checkSourceDealsDamageToPlayerTriggers(gd, dealer,
                player1.getId(), player2.getId(), 2));

        assertThat(entriesOf("Damage Dealer")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"whenever a creature deals combat damage to you\" abilities are two stack objects")
    void twoCombatDamageToYouAbilitiesAreTwoStackObjects() {
        Card card = watcher("Damage Watcher");
        card.addTrigger(Triggers.creatureDealsCombatDamageToYou(), new DrawCardEffect(1));
        card.addTrigger(Triggers.creatureDealsCombatDamageToYou(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> damageCollectors().checkDamageDealtToControllerTriggers(gd, player1.getId(),
                bears.getId(), true));

        assertThat(entriesOf("Damage Watcher")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("Two \"one or more creatures deal combat damage to you\" abilities each trigger once for the step")
    void twoOneOrMoreCombatDamageToYouAbilitiesEachTriggerOnce() {
        Card card = watcher("Batch Damage Watcher");
        card.addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE,
                new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureDealsCombatDamageToYou(), TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(2));
        addCreatureReady(player1, card);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        HashSet<String> firedThisStep = new HashSet<>();

        harness.inMutationScope(() -> {
            damageCollectors().checkDamageDealtToControllerTriggers(gd, player1.getId(), first.getId(), true,
                    firedThisStep);
            damageCollectors().checkDamageDealtToControllerTriggers(gd, player1.getId(), second.getId(), true,
                    firedThisStep);
        });

        assertThat(entriesOf("Batch Damage Watcher")).hasSize(2);
        assertThat(entriesOf("Batch Damage Watcher").stream().map(entry -> entry.getEffectsToResolve().size()))
                .containsExactlyInAnyOrder(2, 1);
    }

    @Test
    @DisplayName("\"Whenever you're dealt combat damage, draw a card and gain 1 life\" is one stack object")
    void controllerDealtCombatDamageAbilityIsOneStackObject() {
        Card card = watcher("Combat Damage Watcher");
        card.addTrigger(Triggers.youAreDealtCombatDamage(), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);

        harness.inMutationScope(() -> damageCollectors().checkControllerDealtCombatDamageTriggers(gd, player1.getId(), 3));

        assertThat(entriesOf("Combat Damage Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Whenever a creature an opponent controls is dealt damage, draw a card and gain 1 life\" is one object")
    void opponentCreatureDealtDamageAbilityIsOneStackObject() {
        Card card = watcher("Pain Watcher");
        card.addTrigger(Triggers.creatureOpponentControlsDealtDamage(), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);

        harness.inMutationScope(() -> damageCollectors().checkOpponentCreatureDealtDamageTriggers(gd, player2.getId()));

        assertThat(entriesOf("Pain Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("An excess-damage ability of two effects is one stack object that names the damaged permanent")
    void excessDamageAbilityIsOneStackObject() {
        Card card = watcher("Excess Watcher");
        card.addTrigger(Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessDamage(),
                new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> damageCollectors().checkOpponentPermanentDealtExcessDamageTriggers(gd, bears,
                player2.getId(), 2, true));

        assertThat(entriesOf("Excess Watcher")).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(bears.getId());
        });
    }

    @Test
    @DisplayName("Two \"when this becomes the target of a spell\" abilities are two stack objects")
    void twoBecomesTargetOfSpellAbilitiesAreTwoStackObjects() {
        Card card = watcher("Spell Target");
        card.addTrigger(Triggers.becomesTargetOfSpell(), new DrawCardEffect(1));
        card.addTrigger(Triggers.becomesTargetOfSpell(), new GainLifeEffect(1));
        Permanent target = addCreatureReady(player1, card);
        StackEntry spell = opponentSpellTargeting(target.getId());

        harness.inMutationScope(() -> collectors().checkBecomesTargetOfSpellTriggers(gd, spell));

        assertThat(entriesOf("Spell Target")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("Two \"when this becomes the target of a spell or ability\" abilities are two stack objects")
    void twoBecomesTargetOfSpellOrAbilityAbilitiesAreTwoStackObjects() {
        Card card = watcher("Any Target");
        card.addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new DrawCardEffect(1));
        card.addTrigger(Triggers.becomesTargetOfSpellOrAbility(), new GainLifeEffect(1));
        Permanent target = addCreatureReady(player1, card);
        StackEntry spell = opponentSpellTargeting(target.getId());

        harness.inMutationScope(() -> collectors().checkBecomesTargetOfSpellTriggers(gd, spell));

        assertThat(entriesOf("Any Target")).hasSize(2)
                .allSatisfy(entry -> assertThat(entry.getEffectsToResolve()).hasSize(1));
    }

    @Test
    @DisplayName("\"Whenever you become the target of a spell, draw a card and gain 1 life\" is one stack object")
    void controllerBecomesTargetOfSpellAbilityIsOneStackObject() {
        Card card = watcher("Player Target Watcher");
        card.addTrigger(Triggers.youBecomeTargetOfSpell(), new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        StackEntry spell = opponentSpellTargeting(player1.getId());

        harness.inMutationScope(() -> collectors().checkBecomesTargetOfSpellTriggers(gd, spell));

        assertThat(entriesOf("Player Target Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"You or a permanent you control becomes the target\" triggers once for each such target")
    void youOrPermanentBecomesTargetTriggersForEachTarget() {
        Card card = watcher("Each Target Watcher");
        card.addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        StackEntry spell = opponentSpellTargeting(first.getId(), second.getId());

        harness.inMutationScope(() -> collectors().checkBecomesTargetOfSpellTriggers(gd, spell));

        assertThat(entriesOf("Each Target Watcher")).hasSize(2);
    }

    @Test
    @DisplayName("\"You and/or at least one permanent you control becomes the target\" triggers once for the spell")
    void youAndOrPermanentBecomesTargetTriggersOnceForTheSpell() {
        Card card = watcher("One Or More Target Watcher");
        card.addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                TriggerOptions.ONE_OR_MORE, new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        StackEntry spell = opponentSpellTargeting(first.getId(), second.getId());

        harness.inMutationScope(() -> collectors().checkBecomesTargetOfSpellTriggers(gd, spell));

        assertThat(entriesOf("One Or More Target Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }
}
