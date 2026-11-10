package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An ability that is only a "you may" is put on the stack the way any other untargeted ability of its trigger is, with
 * the same record of the event (CR 603.5: the choice is made as it resolves), an ability of several effects is one
 * stack object whatever its first effect is (CR 603.3), and two abilities that each choose graveyard targets as they
 * go on the stack both get there (CR 603.3b, 603.3d).
 */
@CardUsed({GrizzlyBears.class})
class LoneMayAbilityRecordTest extends BaseCardTest {

    private static Card creature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private static MayEffect mayDraw() {
        return new MayEffect(new DrawCardEffect(1), "Draw a card?");
    }

    private List<StackEntry> entriesOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).toList();
    }

    private Permanent attacking(Card card) {
        Permanent attacker = addCreatureReady(player1, card);
        attacker.setAttacking(true);
        return attacker;
    }

    @Test
    @DisplayName("\"Whenever this attacks, you may draw a card\" is an ability of an attacking creature that triggered")
    void loneMayAttackAbilityIsSeenByAttackingCreatureAbilityWatchers() {
        Card raider = creature("Curious Raider");
        raider.addTrigger(Triggers.attacks(), mayDraw());
        Card ascension = new Card();
        ascension.setName("Watchful Banner");
        ascension.setType(CardType.ENCHANTMENT);
        ascension.setManaCost("{1}");
        ascension.addTrigger(Triggers.attackingCreatureYouControlAbilityTriggers(), new GainLifeEffect(1));
        addCreatureReady(player1, raider);
        harness.addToBattlefield(player1, ascension);

        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Curious Raider")).hasSize(1);
        assertThat(entriesOf("Watchful Banner")).hasSize(1);
    }

    @Test
    @DisplayName("\"Whenever this attacks, you may draw a card\" records the attack as a mandatory ability does")
    void loneMayAttackAbilityRecordsTheAttack() {
        Card raider = creature("Curious Raider");
        raider.addTrigger(Triggers.attacks(), mayDraw());
        Card soldier = creature("Plain Raider");
        soldier.addTrigger(Triggers.attacks(), new GainLifeEffect(1));
        addCreatureReady(player1, raider);
        addCreatureReady(player1, soldier);

        declareAttackers(player1, List.of(0, 1));

        StackEntry may = entriesOf("Curious Raider").getFirst();
        StackEntry plain = entriesOf("Plain Raider").getFirst();
        assertThat(may.getXValue()).isEqualTo(plain.getXValue()).isEqualTo(2);
        assertThat(may.getSourcePermanentSnapshot()).isNotNull();
        assertThat(may.getDefendingPlayerId()).isEqualTo(plain.getDefendingPlayerId());
    }

    @Test
    @DisplayName("\"Whenever this deals combat damage to a player, you may draw a card\" records the damage as a mandatory ability does")
    void loneMayCombatDamageAbilityRecordsTheDamage() {
        Card raider = creature("Curious Raider");
        raider.addTrigger(Triggers.dealsCombatDamageToPlayer(), mayDraw());
        Card soldier = creature("Plain Raider");
        soldier.addTrigger(Triggers.dealsCombatDamageToPlayer(), new GainLifeEffect(1));
        attacking(raider);
        attacking(soldier);

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);

        StackEntry may = entriesOf("Curious Raider").getFirst();
        StackEntry plain = entriesOf("Plain Raider").getFirst();
        assertThat(may.getEventValue()).isEqualTo(plain.getEventValue()).isEqualTo(2);
        assertThat(may.getTriggeringPlayerId()).isEqualTo(plain.getTriggeringPlayerId());
        assertThat(may.getTargetId()).isEqualTo(plain.getTargetId());
        assertThat(may.isNonTargeting()).isEqualTo(plain.isNonTargeting());
        assertThat(may.getSourcePermanentSnapshot()).isNotNull();
    }

    @Test
    @DisplayName("Two \"exile up to two target cards from that player's graveyard\" combat damage abilities both reach the stack")
    void twoGraveyardChoosingCombatDamageAbilitiesBothReachTheStack() {
        Card first = creature("First Snatcher");
        first.addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileCardsFromGraveyardEffect(2, 0));
        Card second = creature("Second Snatcher");
        second.addTrigger(Triggers.dealsCombatDamageToPlayer(), new ExileCardsFromGraveyardEffect(2, 0));
        attacking(first);
        attacking(second);
        Card bears = new GrizzlyBears();
        Card moreBears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears, moreBears));

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, this::resolveCombat);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(moreBears.getId()));

        assertThat(entriesOf("First Snatcher")).hasSize(1);
        assertThat(entriesOf("Second Snatcher")).hasSize(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("\"When this transforms into [the back face], you may draw a card and you gain 1 life\" is one stack object")
    void transformAbilityWithMayIsOneStackObject() {
        Card back = creature("Risen Shape");
        back.addTrigger(Triggers.transformsToBackFace(ObjectRelation.THIS), mayDraw(), new GainLifeEffect(1));
        Card front = creature("Restless Shape");
        front.setBackFaceCard(back);
        front.addTrigger(Triggers.attacks(), new TransformSelfEffect());
        addCreatureReady(player1, front);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(entriesOf("Risen Shape")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Whenever a creature you control attacks, you may draw a card\" has its owner as its source and the attacker as \"that creature\"")
    void loneMayAllyAttackAbilityHasItsOwnerAsSource() {
        Card marshal = creature("Field Marshal");
        marshal.addTrigger(Triggers.creatureYouControlAttacks(), mayDraw());
        Card sergeant = creature("Drill Sergeant");
        sergeant.addTrigger(Triggers.creatureYouControlAttacks(), new GainLifeEffect(1));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent marshalPermanent = addCreatureReady(player1, marshal);
        addCreatureReady(player1, sergeant);

        declareAttackers(player1, List.of(0));

        StackEntry may = entriesOf("Field Marshal").getFirst();
        StackEntry plain = entriesOf("Drill Sergeant").getFirst();
        assertThat(may.getSourcePermanentId()).isEqualTo(marshalPermanent.getId());
        assertThat(may.getTriggeringPermanentId()).isEqualTo(plain.getTriggeringPermanentId())
                .isEqualTo(attacker.getId());
        assertThat(may.getTargetId()).isEqualTo(plain.getTargetId());
        assertThat(may.getDefendingPlayerId()).isEqualTo(plain.getDefendingPlayerId());
    }
}
