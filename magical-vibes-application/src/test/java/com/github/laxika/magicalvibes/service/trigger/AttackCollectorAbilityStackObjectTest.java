package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@code CombatAttackService}'s attack collectors put each triggered ability on the stack as one object of all its
 * effects (CR 603.3): a "you may" among an ability's effects stays in it and is asked as the ability resolves
 * (CR 603.5), a target is chosen once for the whole ability (CR 603.3d), and two abilities of one permanent or card on
 * one trigger are two objects (CR 603.3b), whoever gave them to it.
 */
@CardUsed({GrizzlyBears.class})
class AttackCollectorAbilityStackObjectTest extends BaseCardTest {

    private static Card watcher(String name) {
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

    @Test
    @DisplayName("\"Whenever this attacks, you may draw a card and you gain 1 life\" is one stack object")
    void selfAttackAbilityWithMayIsOneStackObject() {
        Card card = watcher("Generous Raider");
        card.addTrigger(Triggers.attacks(), mayDraw(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        harness.setHand(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Generous Raider")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An attack ability of two effects a creature has until end of turn is one stack object")
    void temporaryAttackAbilityIsOneStackObject() {
        Permanent raider = addCreatureReady(player1, watcher("Borrowed Raider"));
        raider.addTemporaryAbility(new TriggeredAbility(Triggers.attacks(), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new DrawCardEffect(1)), new EffectRegistration(new GainLifeEffect(1)))));

        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Borrowed Raider")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("An attack ability of two effects a static ability grants is one stack object")
    void grantedAttackAbilityIsOneStackObject() {
        Card card = watcher("Drilled Raider");
        card.addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.attacks(),
                TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new DrawCardEffect(1)), new EffectRegistration(new GainLifeEffect(1)))),
                GrantScope.ALL_OWN_CREATURES));
        addCreatureReady(player1, card);

        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Drilled Raider")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("An attack ability with a graveyard target and another attack ability are two stack objects")
    void selfAttackAbilityWithGraveyardTargetStaysApartFromAnotherAbility() {
        Card card = watcher("Grave Raider");
        card.addTrigger(Triggers.attacks(), returnTargetCreatureCardToHand());
        card.addTrigger(Triggers.attacks(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        declareAttackers(player1, List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(effectCountsOf("Grave Raider")).containsExactlyInAnyOrder(1, 1);
    }

    @Test
    @DisplayName("A printed and a granted \"whenever you attack\" ability are two stack objects")
    void printedAndGrantedYouAttackAbilitiesAreTwoStackObjects() {
        Card card = watcher("War Drummer");
        card.addTrigger(Triggers.youAttack(), new DrawCardEffect(1));
        card.addStaticEffect(new GrantTriggeredAbilityEffect(new TriggeredAbility(Triggers.youAttack(),
                TriggerOptions.DEFAULT, List.of(new EffectRegistration(new GainLifeEffect(1)))),
                GrantScope.ALL_OWN_CREATURES));
        addCreatureReady(player1, card);

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("War Drummer")).containsExactly(1, 1);
    }

    @Test
    @DisplayName("A targeted \"whenever you attack\" ability that triggers only once each turn is used once it triggers")
    void targetedYouAttackAbilityUsesItsOnceEachTurnLimit() {
        Card card = watcher("Patient Sniper");
        card.addTrigger(Triggers.youAttack(), TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                new DestroyTargetPermanentEffect());
        Permanent sniper = addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(entriesOf("Patient Sniper")).hasSize(1);
        harness.passBothPriorities();
        sniper.untap();
        sniper.setAttacking(false);
        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Patient Sniper")).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Two \"whenever you attack a player\" abilities are two stack objects")
    void twoYouAttackPlayerAbilitiesAreTwoStackObjects() {
        Card card = watcher("Siege Herald");
        card.addTrigger(Triggers.youAttackPlayer(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.youAttackPlayer(), new GainLifeEffect(2));
        addCreatureReady(player1, card);

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Siege Herald")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("\"Whenever a creature you control attacks, you may draw a card and you gain 1 life\" is one stack object")
    void allyCreatureAttacksAbilityWithMayIsOneStackObject() {
        Card card = watcher("Field Marshal");
        card.addTrigger(Triggers.creatureYouControlAttacks(), mayDraw(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        harness.setHand(player1, List.of());
        int lifeBefore = gd.getLife(player1.getId());

        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Field Marshal")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("An ability of two effects a permanent has until end of turn for \"a creature you control attacks\" is one stack object")
    void temporaryAllyCreatureAttacksAbilityIsOneStackObject() {
        Permanent marshal = addCreatureReady(player1, watcher("Acting Marshal"));
        marshal.addTemporaryAbility(new TriggeredAbility(Triggers.creatureYouControlAttacks(), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new DrawCardEffect(1)), new EffectRegistration(new GainLifeEffect(1)))));
        marshal.addTemporaryAbility(new TriggeredAbility(Triggers.creatureYouControlAttacks(), TriggerOptions.DEFAULT,
                List.of(new EffectRegistration(new GainLifeEffect(2)))));

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Acting Marshal")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever a creature attacks\" abilities are two stack objects")
    void twoAnyCreatureAttacksAbilitiesAreTwoStackObjects() {
        Card card = watcher("Wall Watcher");
        card.addTrigger(Triggers.creatureAttacks(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creatureAttacks(), new GainLifeEffect(2));
        addCreatureReady(player2, card);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Wall Watcher")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever one or more creatures attack you\" abilities are two stack objects")
    void twoCreaturesAttackYouAbilitiesAreTwoStackObjects() {
        Card card = watcher("Gate Warden");
        card.addTrigger(Triggers.creaturesAttackYou(), new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.creaturesAttackYou(), new GainLifeEffect(2));
        addCreatureReady(player2, card);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Gate Warden")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("A \"whenever you attack\" ability of two effects of a card in a graveyard is one stack object")
    void graveyardYouAttackAbilityIsOneStackObject() {
        Card card = watcher("Buried Drummer");
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new DrawCardEffect(1), new GainLifeEffect(1));
        harness.setGraveyard(player1, List.of(card));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(entriesOf("Buried Drummer")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"whenever a creature attacks\" abilities of a card in a graveyard are two stack objects")
    void twoGraveyardCreatureAttacksAbilitiesAreTwoStackObjects() {
        Card card = watcher("Buried Watcher");
        card.addTrigger(new TriggerCondition(Triggers.creatureAttacks(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(new TriggerCondition(Triggers.creatureAttacks(), AbilityZones.GRAVEYARD),
                TriggerOptions.DEFAULT, new GainLifeEffect(2));
        harness.setGraveyard(player1, List.of(card));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Buried Watcher")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever you attack\" abilities of a card in the command zone are two stack objects")
    void twoCommandZoneYouAttackAbilitiesAreTwoStackObjects() {
        Card card = watcher("Distant General");
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.COMMAND), TriggerOptions.DEFAULT,
                new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.COMMAND), TriggerOptions.DEFAULT,
                new GainLifeEffect(2));
        gd.playerCommandZones.computeIfAbsent(player1.getId(), ignored -> new ArrayList<>()).add(card);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Distant General")).containsExactly(2, 1);
    }

    @Test
    @DisplayName("Two \"whenever you attack\" abilities of a card in exile are two stack objects")
    void twoExileYouAttackAbilitiesAreTwoStackObjects() {
        Card card = watcher("Exiled General");
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.EXILE), TriggerOptions.DEFAULT,
                new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(new TriggerCondition(Triggers.youAttack(), AbilityZones.EXILE), TriggerOptions.DEFAULT,
                new GainLifeEffect(2));
        gd.exiledCards.add(new ExiledCardEntry(card, player1.getId(), null));
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        assertThat(effectCountsOf("Exiled General")).containsExactly(2, 1);
    }
}
