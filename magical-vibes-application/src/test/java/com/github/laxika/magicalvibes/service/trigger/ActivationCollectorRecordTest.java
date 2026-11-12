package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The one activation collector ({@code TriggerCollectionService.collectActivationTriggers}): every "whenever [a
 * player] activates an ability" ability triggers once the activated ability is on the stack and its costs are paid
 * (CR 602.2b, CR 601.2i), and its entry records the activation the same way whatever its effects are. An ability
 * that copies the activated ability and one that gains life have the same record, and the stack-entry and payment
 * filters of the trigger are judged for both.
 */
@CardUsed({GrizzlyBears.class})
class ActivationCollectorRecordTest extends BaseCardTest {

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        if (type == CardType.CREATURE) {
            card.setPower(2);
            card.setToughness(4);
        }
        return card;
    }

    private Permanent watcher(String name, Trigger trigger, CardEffect effect) {
        Card card = card(name, CardType.ENCHANTMENT);
        card.addTrigger(trigger, effect);
        return harness.addToBattlefieldAndReturn(player1, card);
    }

    private Permanent pinger(CardEffect... costs) {
        Card pinger = card("Pinger", CardType.CREATURE);
        List<CardEffect> effects = new java.util.ArrayList<>(List.of(costs));
        effects.add(new DealDamageToAnyTargetEffect(1));
        pinger.addActivatedAbility(new ActivatedAbility(true, null, effects,
                "{T}: This creature deals 1 damage to any target."));
        return addCreatureReady(player1, pinger);
    }

    private void activate(Permanent source, UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, targetId);
    }

    private List<StackEntry> triggersOf(String name) {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals(name))
                .toList();
    }

    @Test
    @DisplayName("An ability that copies the activated ability and one that doesn't record the activation alike")
    void everyActivationAbilityRecordsTheActivation() {
        watcher("Plain Watcher", Triggers.youActivateNonManaAbility(), new GainLifeEffect(1));
        watcher("Copy Watcher", Triggers.youActivateNonManaAbility(), new CopyTriggeringSpellOrAbilityEffect());
        Permanent source = pinger();

        activate(source, player2.getId());

        assertThat(triggersOf("Plain Watcher")).hasSize(1);
        assertThat(triggersOf("Copy Watcher")).hasSize(1);
        for (StackEntry entry : List.of(triggersOf("Plain Watcher").getFirst(),
                triggersOf("Copy Watcher").getFirst())) {
            assertThat(entry.getTriggeringSpellSnapshot()).isNotNull();
            assertThat(entry.getTriggeringSpellSnapshot().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(entry.getTriggeringSpellSnapshot().getTargetId()).isEqualTo(player2.getId());
            assertThat(entry.getTriggeringActivatedAbility())
                    .isSameAs(source.getCard().getActivatedAbilities().getFirst());
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(source.getId());
            assertThat(entry.getTriggeringPermanentControllerId()).isEqualTo(player1.getId());
        }
    }

    @Test
    @DisplayName("\"If life was paid to activate it\" is judged for an ability that doesn't copy, with the life paid")
    void paymentFilterIsJudgedWhateverTheEffectsAre() {
        watcher("Life Watcher", Triggers.youActivateNonManaAbility()
                .withPayment(AbilityTrigger.ActivationPayment.LIFE), new GainLifeEffect(1));
        Permanent paying = pinger(new PayLifeCost(2));
        Permanent free = pinger();

        activate(free, player2.getId());
        assertThat(triggersOf("Life Watcher")).isEmpty();

        activate(paying, player2.getId());
        assertThat(triggersOf("Life Watcher")).hasSize(1);
        assertThat(triggersOf("Life Watcher").getFirst().getEventValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("\"That targets a player\" is judged for an ability that doesn't copy")
    void stackEntryFilterIsJudgedWhateverTheEffectsAre() {
        watcher("Target Watcher", Triggers.youActivateNonManaAbility()
                .withStackEntryFilter(new StackEntryTargetsAnyPlayerPredicate()), new GainLifeEffect(1));
        Permanent first = pinger();
        Permanent second = pinger();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activate(first, bears.getId());
        assertThat(triggersOf("Target Watcher")).isEmpty();

        activate(second, player2.getId());
        assertThat(triggersOf("Target Watcher")).hasSize(1);
    }

    @Test
    @DisplayName("An ability on \"an ability that isn't a mana ability\" that targets a permanent is asked for it")
    void permanentTargetIsChosenAsTheAbilityGoesOnTheStack() {
        Card card = card("Destroy Watcher", CardType.ENCHANTMENT);
        CardEffect destroy = new DestroyTargetPermanentEffect();
        card.addTrigger(Triggers.youActivateNonManaAbility(), destroy);
        card.target(TargetFilters.creature()).bindEffect(destroy);
        harness.addToBattlefield(player1, card);
        Permanent source = pinger();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        activate(source, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(triggersOf("Destroy Watcher")).hasSize(1);
        StackEntry entry = triggersOf("Destroy Watcher").getFirst();
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
        assertThat(entry.getTriggeringActivatedAbility())
                .isSameAs(source.getCard().getActivatedAbilities().getFirst());
        assertThat(entry.getTriggeringPermanentId()).isEqualTo(source.getId());
    }

    @Test
    @DisplayName("An ability activated from a graveyard is an activated ability: \"whenever you activate an ability\"")
    void graveyardAbilityTriggersAnyAbilityWatcher() {
        watcher("Any Watcher", Triggers.youActivateAbility(), new GainLifeEffect(1));
        watcher("Creature Watcher", Triggers.youActivateAbility().withSource(new PermanentIsCreaturePredicate()),
                new GainLifeEffect(1));
        Card eternal = card("Eternal", CardType.CREATURE);
        eternal.addEternalize("{1}", "Cat");
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        graveyard.add(eternal);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, graveyard.indexOf(eternal));

        assertThat(triggersOf("Any Watcher")).hasSize(1);
        assertThat(triggersOf("Any Watcher").getFirst().getTriggeringSpellSnapshot().getCard()).isSameAs(eternal);
        assertThat(triggersOf("Creature Watcher")).isEmpty();
    }
}
