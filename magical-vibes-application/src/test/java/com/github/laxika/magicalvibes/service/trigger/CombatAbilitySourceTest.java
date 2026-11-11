package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
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
 * A combat collector reads every ability its permanent has for the watched event, whoever gave it to the permanent
 * and for however long (CR 603.2), and each printed ability by its own trigger conditions; an ability whose mode is
 * chosen as it triggers in the declare blockers step has it chosen then (CR 603.3c), on an entry that records the
 * block as any other ability's does.
 */
@CardUsed({GrizzlyBears.class})
class CombatAbilitySourceTest extends BaseCardTest {

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

    private static TriggeredAbility ability(Trigger trigger, CardEffect effect) {
        return new TriggeredAbility(trigger, TriggerOptions.DEFAULT, List.of(new EffectRegistration(effect)));
    }

    private static ChooseOneAtTriggerTimeEffect gainOrDraw() {
        return new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("You gain 1 life", new GainLifeEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)))));
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

    private void declareNoBlockers() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
    }

    @Test
    @DisplayName("A \"deals damage to a player\" ability a creature has until end of turn triggers on its combat damage")
    void temporaryDealsDamageToPlayerAbilityTriggersOnCombatDamage() {
        Permanent raider = attacking(creature("Borrowed Raider"));
        raider.addTemporaryAbility(ability(Triggers.dealsDamageToPlayer(), new GainLifeEffect(1)));

        dealCombatDamage();

        assertThat(entriesOf("Borrowed Raider")).hasSize(1);
    }

    @Test
    @DisplayName("\"Attacks and isn't blocked\" abilities a creature has until end of turn and until it leaves both trigger")
    void temporaryAndPersistentUnblockedAttackAbilitiesTrigger() {
        Permanent raider = attacking(creature("Slippery Raider"));
        raider.addTemporaryAbility(ability(Triggers.attacksUnblocked(), new GainLifeEffect(1)));
        raider.addPersistentAbility(ability(Triggers.attacksUnblocked(), new DrawCardEffect(1)));

        declareNoBlockers();

        assertThat(entriesOf("Slippery Raider")).hasSize(2);
    }

    @Test
    @DisplayName("A \"creature you control attacks and isn't blocked\" ability a permanent has until end of turn triggers")
    void temporaryAllyUnblockedAttackAbilityTriggers() {
        Permanent lookout = addCreatureReady(player1, creature("Lookout"));
        lookout.addTemporaryAbility(ability(Triggers.creatureYouControlAttacksUnblocked(), new GainLifeEffect(1)));
        attacking(new GrizzlyBears());

        declareNoBlockers();

        assertThat(entriesOf("Lookout")).hasSize(1);
    }

    @Test
    @DisplayName("\"Whenever this creature blocks, choose one\" has its mode chosen as it triggers, and records the block")
    void blockAbilityModeIsChosenAsItTriggers() {
        Card card = creature("Choosy Guard");
        card.addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, gainOrDraw());
        Permanent attacker = attacking(new GrizzlyBears());
        addCreatureReady(player2, card);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handleListChoice(player2, "You gain 1 life");

        assertThat(entriesOf("Choosy Guard")).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).singleElement().isInstanceOf(GainLifeEffect.class);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(attacker.getId());
            assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
            assertThat(entry.isNonTargeting()).isTrue();
        });
    }

    @Test
    @DisplayName("\"Whenever equipped creature becomes blocked, choose one\" has its mode chosen as it triggers")
    void attachedBecomesBlockedAbilityModeIsChosenAsItTriggers() {
        Card gear = permanent("Choosy Gear", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        gear.addTrigger(Triggers.becomesBlocked(), gainOrDraw());
        Permanent attacker = attacking(new GrizzlyBears());
        addCreatureReady(player1, gear).setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.handleListChoice(player1, "Draw a card");

        assertThat(entriesOf("Choosy Gear")).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).singleElement().isInstanceOf(DrawCardEffect.class);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(attacker.getId());
        });
    }

    @Test
    @DisplayName("An Equipment's \"deals combat damage to a player\" ability triggers beside its \"equipped creature\" one")
    void attachedCollectorReadsEachPrintedAbilityByItsOwnConditions() {
        Card gear = permanent("Twin Gear", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        gear.addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(1));
        gear.addTrigger(Triggers.equippedCreatureDealsCombatDamageToPlayer(), new DrawCardEffect(1));
        Card armor = permanent("Guard Dog Armor", CardType.ARTIFACT, CardSubtype.EQUIPMENT);
        armor.addTrigger(List.of(Triggers.dealsCombatDamageToPlayer(),
                Triggers.equippedCreatureDealsCombatDamageToPlayer()), new GainLifeEffect(1));
        Permanent attacker = attacking(new GrizzlyBears());
        addCreatureReady(player1, gear).setAttachedTo(attacker.getId());
        addCreatureReady(player1, armor).setAttachedTo(attacker.getId());

        dealCombatDamage();

        assertThat(entriesOf("Twin Gear")).hasSize(2);
        assertThat(entriesOf("Guard Dog Armor")).hasSize(1);
    }
}
