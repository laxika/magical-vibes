package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code CombatDamageService}'s combat-damage collectors put each triggered ability on the stack as one object of all
 * its effects (CR 603.3): a "you may" among an ability's effects stays in it and is asked as the ability resolves
 * (CR 603.5), a target is chosen once for the whole ability (CR 603.3d), and two abilities of one permanent or card on
 * one trigger are two objects (CR 603.3b), whoever gave them to it. An ability's limit, "one or more" and intervening
 * "if" are judged and marked once, for the ability (CR 603.2h, CR 603.4).
 */
@CardUsed({GrizzlyBears.class, Divination.class})
class CombatDamageCollectorAbilityStackObjectTest extends BaseCardTest {

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static Card attachment(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        card.setManaCost("{1}");
        return card;
    }

    private static TriggeredAbility drawAndGain(Trigger trigger) {
        return new TriggeredAbility(trigger, TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new DrawCardEffect(1)), new EffectRegistration(new GainLifeEffect(1))));
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private List<Integer> effectCountsOf(String name) {
        return entriesOf(name).stream().map(entry -> entry.getEffectsToResolve().size()).toList();
    }

    private Permanent attacking(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    /** Deals the combat damage of the attackers set up, unblocked, and stops with its triggers on the stack. */
    private void dealCombatDamage() {
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
    }

    /** Has {@code player2}'s first creature block {@code player1}'s first, then deals the combat damage. */
    private void blockAndDealCombatDamage() {
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
    }

    @Test
    @DisplayName("\"Whenever this deals combat damage to a player, draw a card and you gain 1 life\" is one stack object")
    void combatDamageToPlayerAbilityIsOneStackObject() {
        Card card = creature("Keen Raider");
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        attacking(card);

        dealCombatDamage();

        assertThat(effectCountsOf("Keen Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("Two \"deals combat damage to a player\" abilities are two stack objects, each with all its effects")
    void twoCombatDamageToPlayerAbilitiesAreTwoStackObjects() {
        Card card = creature("Twin Raider");
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(2));
        attacking(card);

        dealCombatDamage();

        assertThat(effectCountsOf("Twin Raider")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("\"Deals combat damage to a player, you may draw a card and you gain 1 life\" is one stack object")
    void mayAmongACombatDamageAbilitysEffectsStaysInIt() {
        Card card = creature("Choosy Raider");
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(new DrawCardEffect(1), "Draw a card?"),
                new GainLifeEffect(1));
        attacking(card);

        dealCombatDamage();

        assertThat(effectCountsOf("Choosy Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("\"Deals combat damage to a player, destroy target creature and you gain 1 life\" is one stack object")
    void targetedCombatDamageAbilityTakesItsOtherEffectIntoItsOneObject() {
        Card card = creature("Deadly Raider");
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new DestroyTargetPermanentEffect(), new GainLifeEffect(1));
        attacking(card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        dealCombatDamage();
        assertThat(entriesOf("Deadly Raider")).isEmpty();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(effectCountsOf("Deadly Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("A \"deals damage to a player\" ability of two effects is one stack object for combat damage")
    void damageToPlayerAbilityIsOneStackObjectForCombatDamage() {
        Card card = creature("Blunt Raider");
        card.addTrigger(Triggers.dealsDamageToPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        attacking(card);

        dealCombatDamage();

        assertThat(effectCountsOf("Blunt Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("A combat damage ability of two effects a creature has until end of turn is one stack object")
    void temporaryCombatDamageToPlayerAbilityIsOneStackObject() {
        Permanent raider = attacking(creature("Drilled Raider"));
        raider.addTemporaryAbility(drawAndGain(Triggers.dealsCombatDamageToPlayer()));

        dealCombatDamage();

        assertThat(effectCountsOf("Drilled Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("A combat damage ability of two effects a static ability grants is one stack object")
    void staticallyGrantedCombatDamageToPlayerAbilityIsOneStackObject() {
        Card banner = attachment("Raid Banner", CardType.ENCHANTMENT);
        banner.addStaticEffect(new GrantTriggeredAbilityEffect(drawAndGain(Triggers.dealsCombatDamageToPlayer()),
                GrantScope.OWN_CREATURES));
        addCreatureReady(player1, banner);
        attacking(creature("Bannered Raider"));

        dealCombatDamage();

        assertThat(effectCountsOf("Bannered Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("A target chosen at random is the one target of every effect of its ability")
    void randomlyTargetedCombatDamageAbilityIsOneStackObject() {
        Divination divination = new Divination();
        harness.setGraveyard(player1, List.of(divination));
        Card card = creature("Lucky Raider");
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileRandomInstantOrSorceryFromGraveyardAndCastNextUpkeepEffect(), new GainLifeEffect(1));
        attacking(card);

        dealCombatDamage();

        assertThat(entriesOf("Lucky Raider")).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getTargetId()).isEqualTo(divination.getId());
        });
    }

    @Test
    @DisplayName("No effect is dropped from a combat damage ability: only its intervening \"if\" keeps it off the stack")
    void combatDamageAbilityKeepsEveryEffectUnlessItsInterveningIfFails() {
        Card unconditional = creature("Famous Raider");
        unconditional.addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1), new GainLifeEffect(1));
        attacking(unconditional).setRenowned(true);
        Card conditional = creature("Humble Raider");
        conditional.addTrigger(Triggers.dealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new SourceIsRenowned())),
                new RenownEffect(1), new GainLifeEffect(1));
        attacking(conditional).setRenowned(true);

        dealCombatDamage();

        assertThat(effectCountsOf("Famous Raider")).containsExactly(2);
        assertThat(entriesOf("Humble Raider")).isEmpty();
    }

    @Test
    @DisplayName("Two \"equipped creature deals combat damage to a player\" abilities of an Equipment are two objects")
    void twoAttachedCombatDamageAbilitiesAreTwoStackObjects() {
        Card card = attachment("Twin Blade", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(2));
        Permanent attacker = attacking(new GrizzlyBears());
        Permanent blade = addCreatureReady(player1, card);
        blade.setAttachedTo(attacker.getId());

        dealCombatDamage();

        assertThat(effectCountsOf("Twin Blade")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("\"Whenever a creature deals combat damage to enchanted player, ...\" of two effects is one object")
    void curseCombatDamageAbilityIsOneStackObject() {
        Card card = attachment("Open Wound", CardType.ENCHANTMENT, CardSubtype.AURA, CardSubtype.CURSE);
        card.setEnchantPlayer(true);
        card.addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        Permanent curse = addCreatureReady(player1, card);
        curse.setAttachedTo(player2.getId());
        attacking(new GrizzlyBears());

        dealCombatDamage();

        assertThat(effectCountsOf("Open Wound")).containsExactly(2);
    }

    @Test
    @DisplayName("\"Whenever a creature with the same name deals combat damage to a player, ...\" is one object")
    void sameNameCombatDamageAbilityIsOneStackObject() {
        Card watcher = creature("Echo Raider");
        watcher.addTrigger(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(), new DrawCardEffect(1),
                new GainLifeEffect(1));
        addCreatureReady(player2, watcher);
        attacking(creature("Echo Raider"));

        dealCombatDamage();

        assertThat(effectCountsOf("Echo Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("\"Whenever a creature you control deals combat damage to a player, ...\" of two effects is one object")
    void allyCombatDamageAbilityIsOneStackObject() {
        Card card = creature("Raid Captain");
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), new DrawCardEffect(1),
                new GainLifeEffect(1));
        addCreatureReady(player1, card);
        attacking(new GrizzlyBears());

        dealCombatDamage();

        assertThat(effectCountsOf("Raid Captain")).containsExactly(2);
    }

    @Test
    @DisplayName("Two \"one or more creatures you control deal combat damage\" abilities each trigger once, whole")
    void twoOneOrMoreAllyCombatDamageAbilitiesEachTriggerOnceWithAllTheirEffects() {
        Card card = creature("Raid Marshal");
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), TriggerOptions.ONE_OR_MORE,
                new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(), TriggerOptions.ONE_OR_MORE,
                new GainLifeEffect(2));
        addCreatureReady(player1, card);
        attacking(new GrizzlyBears());
        attacking(new GrizzlyBears());

        dealCombatDamage();

        assertThat(effectCountsOf("Raid Marshal")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("An ally combat damage ability that triggers only once each turn goes on the stack with all its effects")
    void onceEachTurnAllyCombatDamageAbilityKeepsItsLaterEffects() {
        Card card = creature("Raid Herald");
        card.addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN), new DrawCardEffect(1),
                new GainLifeEffect(1));
        addCreatureReady(player1, card);
        attacking(new GrizzlyBears());
        attacking(new GrizzlyBears());

        dealCombatDamage();

        assertThat(effectCountsOf("Raid Herald")).containsExactly(2);
    }

    @Test
    @DisplayName("A graveyard \"a creature you control deals combat damage to a player\" ability of two effects is one object")
    void graveyardAllyCombatDamageAbilityIsOneStackObject() {
        Card card = creature("Buried Raider");
        card.addTriggeredAbility(new TriggeredAbility(
                new TriggerCondition(Triggers.creatureYouControlDealsCombatDamageToPlayer(), AbilityZones.GRAVEYARD),
                new DrawCardEffect(1), new GainLifeEffect(1)));
        harness.setGraveyard(player1, List.of(card));
        attacking(new GrizzlyBears());

        dealCombatDamage();

        assertThat(effectCountsOf("Buried Raider")).containsExactly(2);
    }

    @Test
    @DisplayName("Two graveyard \"combat damage is dealt to you\" abilities are two stack objects")
    void twoGraveyardDealtCombatDamageAbilitiesAreTwoStackObjects() {
        Card card = creature("Buried Avenger");
        TriggerCondition dealtCombatDamage =
                new TriggerCondition(Triggers.youOrYourPlaneswalkerDealtCombatDamage(), AbilityZones.GRAVEYARD);
        card.target(TargetFilters.attackingCreature())
                .addTrigger(dealtCombatDamage, TriggerOptions.DEFAULT, new DestroyTargetPermanentEffect());
        card.target(TargetFilters.attackingCreature())
                .addTrigger(dealtCombatDamage, TriggerOptions.DEFAULT, new DestroyTargetPermanentEffect());
        harness.setGraveyard(player2, List.of(card));
        Permanent first = attacking(new GrizzlyBears());
        Permanent second = attacking(new GrizzlyBears());

        dealCombatDamage();
        harness.handlePermanentChosen(player2, first.getId());
        harness.handlePermanentChosen(player2, second.getId());

        assertThat(effectCountsOf("Buried Avenger")).containsExactly(1, 1);
    }

    @Test
    @DisplayName("\"Whenever this deals combat damage to a creature, draw a card and you gain 1 life\" is one object")
    void combatDamageToCreatureAbilityIsOneStackObject() {
        Card card = creature("Keen Duelist");
        card.setToughness(5);
        card.addTrigger(Triggers.dealsCombatDamageToCreature(), new DrawCardEffect(1), new GainLifeEffect(1));
        attacking(card);
        addCreatureReady(player2, new GrizzlyBears());

        blockAndDealCombatDamage();

        assertThat(effectCountsOf("Keen Duelist")).containsExactly(2);
    }

    @Test
    @DisplayName("\"Deals combat damage to one or more blocking creatures, ...\" of two effects is one object")
    void combatDamageToBlockingCreatureAbilityIsOneStackObject() {
        Card card = creature("Keen Breaker");
        card.setToughness(5);
        card.addTrigger(Triggers.dealsCombatDamageToBlockingCreature(), new DrawCardEffect(1), new GainLifeEffect(1));
        attacking(card);
        addCreatureReady(player2, new GrizzlyBears());

        blockAndDealCombatDamage();

        assertThat(effectCountsOf("Keen Breaker")).containsExactly(2);
    }

    @Test
    @DisplayName("\"Whenever this is dealt combat damage, draw a card and you gain 1 life\" is one stack object")
    void dealtCombatDamageAbilityIsOneStackObject() {
        Card card = creature("Thick Wall");
        card.setToughness(5);
        card.addTrigger(Triggers.dealtCombatDamage(), new DrawCardEffect(1), new GainLifeEffect(1));
        attacking(new GrizzlyBears());
        addCreatureReady(player2, card);

        blockAndDealCombatDamage();

        assertThat(effectCountsOf("Thick Wall")).containsExactly(2);
    }

    @Test
    @DisplayName("A \"deals combat damage\" ability of two effects a creature has until end of turn is one stack object")
    void temporaryDealsCombatDamageAbilityIsOneStackObject() {
        Permanent raider = attacking(creature("Lent Raider"));
        raider.addTemporaryAbility(drawAndGain(Triggers.dealsCombatDamage()));

        dealCombatDamage();

        assertThat(effectCountsOf("Lent Raider")).containsExactly(2);
    }
}
