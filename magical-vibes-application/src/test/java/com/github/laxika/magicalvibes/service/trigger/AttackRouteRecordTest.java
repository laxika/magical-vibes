package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An attack ability records the attack the same way whichever route puts it on the stack: an ability whose mode,
 * graveyard card or target is chosen as it triggers (CR 603.3c, 603.3d) carries what its collector records for one
 * that chooses nothing.
 */
@CardUsed({GrizzlyBears.class})
class AttackRouteRecordTest extends BaseCardTest {

    private static Card card(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setSubtypes(List.of(subtypes));
        card.setManaCost("{1}");
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(2);
        }
        return card;
    }

    private static Card plain(String name, CardType type, Trigger trigger, CardSubtype... subtypes) {
        Card card = card(name, type, subtypes);
        card.addTrigger(trigger, new GainLifeEffect(1));
        return card;
    }

    private static Card modal(String name, CardType type, Trigger trigger, CardSubtype... subtypes) {
        Card card = card(name, type, subtypes);
        card.addTrigger(trigger, new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("You gain 1 life", new GainLifeEffect(1)),
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1))))));
        return card;
    }

    private static Card graveyard(String name, CardType type, Trigger trigger, CardSubtype... subtypes) {
        Card card = card(name, type, subtypes);
        card.addTrigger(trigger, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardTypePredicate(CardType.CREATURE))
                .targetGraveyard(true)
                .build());
        return card;
    }

    private static Card targeted(String name, CardType type, Trigger trigger, CardSubtype... subtypes) {
        Card card = card(name, type, subtypes);
        CardEffect boost = new BoostTargetCreatureEffect(1, 1);
        card.addTrigger(trigger, boost);
        card.target(TargetFilters.creature()).bindEffect(boost);
        return card;
    }

    private StackEntry entryOf(String name) {
        return gd.stack.stream().filter(entry -> entry.getCard().getName().equals(name)).findFirst().orElseThrow();
    }

    private void attackWith(List<Integer> indices) {
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, indices));
    }

    private void assertSameAttachedAttackRecord(StackEntry entry, StackEntry plain, Permanent attacker) {
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(plain.getTriggeringPermanentId())
                .isEqualTo(attacker.getId());
        assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(plain.getTriggeringPermanentControllerId())
                .isEqualTo(player1.getId());
        assertThat(entry.getDefendingPlayerId()).isEqualTo(plain.getDefendingPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getAttackedTargetId()).isEqualTo(plain.getAttackedTargetId()).isEqualTo(player2.getId());
        assertThat(entry.getAttachedPermanentSnapshot()).isNotNull();
        assertThat(entry.getAttachedPermanentSnapshot().getId()).isEqualTo(attacker.getId());
        assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
    }

    @Test
    @DisplayName("\"Whenever equipped creature attacks, choose one\" records the attack as a plain ability does")
    void attachedAttackModalAbilityRecordsTheAttack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, plain("Plain Banner", CardType.ARTIFACT, Triggers.attacks(), CardSubtype.EQUIPMENT))
                .setAttachedTo(attacker.getId());
        addCreatureReady(player1, modal("Choosy Banner", CardType.ARTIFACT, Triggers.attacks(), CardSubtype.EQUIPMENT))
                .setAttachedTo(attacker.getId());

        attackWith(List.of(0));
        harness.handleListChoice(player1, "You gain 1 life");

        assertSameAttachedAttackRecord(entryOf("Choosy Banner"), entryOf("Plain Banner"), attacker);
    }

    @Test
    @DisplayName("\"Whenever equipped creature attacks, return target creature card\" records the attack as a plain ability does")
    void attachedAttackGraveyardAbilityRecordsTheAttack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, plain("Plain Lantern", CardType.ARTIFACT, Triggers.attacks(), CardSubtype.EQUIPMENT))
                .setAttachedTo(attacker.getId());
        addCreatureReady(player1, graveyard("Grave Lantern", CardType.ARTIFACT, Triggers.attacks(),
                CardSubtype.EQUIPMENT)).setAttachedTo(attacker.getId());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        attackWith(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertSameAttachedAttackRecord(entryOf("Grave Lantern"), entryOf("Plain Lantern"), attacker);
    }

    @Test
    @DisplayName("\"Whenever equipped creature attacks, target creature gets +1/+1\" records the defending player")
    void attachedAttackTargetedAbilityRecordsTheDefendingPlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, plain("Plain Pennant", CardType.ARTIFACT, Triggers.attacks(), CardSubtype.EQUIPMENT))
                .setAttachedTo(attacker.getId());
        addCreatureReady(player1, targeted("Aimed Pennant", CardType.ARTIFACT, Triggers.attacks(),
                CardSubtype.EQUIPMENT)).setAttachedTo(attacker.getId());

        attackWith(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());

        assertSameAttachedAttackRecord(entryOf("Aimed Pennant"), entryOf("Plain Pennant"), attacker);
    }

    @Test
    @DisplayName("\"Whenever this attacks, choose one\" records the attackers, the defending player and X as a plain ability does")
    void selfAttackModalAbilityRecordsTheAttack() {
        addCreatureReady(player1, plain("Plain Raider", CardType.CREATURE, Triggers.attacks()));
        addCreatureReady(player1, modal("Choosy Raider", CardType.CREATURE, Triggers.attacks()));

        attackWith(List.of(0, 1));
        harness.handleListChoice(player1, "You gain 1 life");

        StackEntry entry = entryOf("Choosy Raider");
        StackEntry plain = entryOf("Plain Raider");
        assertThat(entry.getAttackingPermanentSnapshots()).hasSize(2).hasSameSizeAs(plain.getAttackingPermanentSnapshots());
        assertThat(entry.getDefendingPlayerId()).isEqualTo(plain.getDefendingPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getAttackedTargetId()).isEqualTo(plain.getAttackedTargetId()).isEqualTo(player2.getId());
        assertThat(entry.getXValue()).isEqualTo(plain.getXValue()).isEqualTo(2);
        assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
    }

    @Test
    @DisplayName("\"Whenever this attacks, return target creature card\" records the attackers, the defending player and X")
    void selfAttackGraveyardAbilityRecordsTheAttack() {
        addCreatureReady(player1, plain("Plain Digger", CardType.CREATURE, Triggers.attacks()));
        addCreatureReady(player1, graveyard("Grave Digger", CardType.CREATURE, Triggers.attacks()));
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        attackWith(List.of(0, 1));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        StackEntry entry = entryOf("Grave Digger");
        StackEntry plain = entryOf("Plain Digger");
        assertThat(entry.getAttackingPermanentSnapshots()).hasSize(2).hasSameSizeAs(plain.getAttackingPermanentSnapshots());
        assertThat(entry.getDefendingPlayerId()).isEqualTo(plain.getDefendingPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getXValue()).isEqualTo(plain.getXValue()).isEqualTo(2);
        assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
    }

    @Test
    @DisplayName("\"Whenever a creature you control attacks, target creature gets +1/+1\" records the defending player and the attacker's owner")
    void allyAttackTargetedAbilityRecordsTheAttack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, plain("Plain Standard", CardType.ENCHANTMENT,
                Triggers.creatureYouControlAttacks()));
        harness.addToBattlefield(player1, targeted("Aimed Standard", CardType.ENCHANTMENT,
                Triggers.creatureYouControlAttacks()));

        attackWith(List.of(0));
        harness.handlePermanentChosen(player1, attacker.getId());

        StackEntry entry = entryOf("Aimed Standard");
        StackEntry plain = entryOf("Plain Standard");
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(plain.getTriggeringPermanentId())
                .isEqualTo(attacker.getId());
        assertThat(entry.getDefendingPlayerId()).isEqualTo(plain.getDefendingPlayerId()).isEqualTo(player2.getId());
        assertThat(entry.getTriggeringPermanentOwnerId()).isEqualTo(plain.getTriggeringPermanentOwnerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("\"Whenever a creature you control attacks, choose one\" records the attack as a plain ability does")
    void allyAttackModalAbilityRecordsTheAttack() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, plain("Plain Herald", CardType.ENCHANTMENT,
                Triggers.creatureYouControlAttacks()));
        harness.addToBattlefield(player1, modal("Choosy Herald", CardType.ENCHANTMENT,
                Triggers.creatureYouControlAttacks()));

        attackWith(List.of(0));
        harness.handleListChoice(player1, "You gain 1 life");

        StackEntry entry = entryOf("Choosy Herald");
        StackEntry plain = entryOf("Plain Herald");
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(plain.getTriggeringPermanentId())
                .isEqualTo(attacker.getId());
        assertThat(entry.getTriggeringPermanentOwnerId()).isEqualTo(plain.getTriggeringPermanentOwnerId())
                .isEqualTo(player1.getId());
        assertThat(entry.getSourcePermanentSnapshot()).isNotNull();
        assertThat(entry.getTargetId()).isEqualTo(plain.getTargetId()).isEqualTo(attacker.getId());
    }
}
