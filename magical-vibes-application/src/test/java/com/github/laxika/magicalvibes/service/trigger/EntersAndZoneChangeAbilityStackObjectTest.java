package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * A permanent's own "when this enters" abilities and the hand-rolled zone-change collectors put each triggered ability
 * on the stack as one object of all its effects (CR 603.3): an ability with a target in a graveyard or on the stack
 * chooses it once, for the whole ability (CR 603.3d), a "you may" among its effects is asked as it resolves
 * (CR 603.5), and two abilities stay two objects.
 */
@CardUsed({GrizzlyBears.class, Shock.class, Forest.class})
class EntersAndZoneChangeAbilityStackObjectTest extends BaseCardTest {

    private static Card watcher(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(2);
        card.setToughness(2);
        return card;
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

    private static PermanentRemovalService removal() {
        return GameTestEngineContext.get().getBean(PermanentRemovalService.class);
    }

    @Test
    @DisplayName("\"When this enters, return target creature card from your graveyard to your hand and draw a card\" is one stack object")
    void entersAbilityWithGraveyardTargetIsOneStackObject() {
        Card card = watcher("Grave Scholar");
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), returnTargetCreatureCardToHand(), new DrawCardEffect(1));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, card);

        assertThat(entriesOf("Grave Scholar")).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        assertThat(entriesOf("Grave Scholar")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(bears);
    }

    @Test
    @DisplayName("\"When this enters, counter target spell and you gain 1 life\" is one stack object")
    void entersAbilityWithSpellTargetIsOneStackObject() {
        Card card = watcher("Spell Warden");
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), new CounterSpellEffect(), new GainLifeEffect(1));
        Shock shock = new Shock();
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, shock, player2.getId(), "Shock", List.of()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.enterBattlefieldAndReturn(player1, card);

        assertThat(entriesOf("Spell Warden")).isEmpty();
        harness.handlePermanentChosen(player1, shock.getId());
        assertThat(entriesOf("Spell Warden")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        assertThat(entriesOf("Shock")).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Two \"when this enters\" abilities, one with a graveyard target, are two stack objects")
    void twoEntersAbilitiesAreTwoStackObjects() {
        Card card = watcher("Grave Twin");
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), returnTargetCreatureCardToHand());
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1), new GainLifeEffect(1));
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.enterBattlefieldAndReturn(player1, card);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(entriesOf("Grave Twin")).hasSize(2);
        assertThat(entriesOf("Grave Twin").stream().map(entry -> entry.getEffectsToResolve().size()))
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("A \"when this enters\" ability with a graveyard target and a permanent target of its own is rejected")
    void entersAbilityWithTwoKindsOfTargetChoiceFails() {
        Card card = watcher("Grave Wrecker");
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), returnTargetCreatureCardToHand(),
                new DestroyTargetPermanentEffect());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.enterBattlefieldAndReturn(player1, card))
                .hasStackTraceContaining("one ability is one stack object");
    }

    @Test
    @DisplayName("A \"when this enters\" ability whose mode is chosen as it triggers, beside another effect, is rejected")
    void triggerTimeModeChoiceBesideAnotherEffectFails() {
        Card card = watcher("Modal Scholar");
        card.addTrigger(Triggers.enters(ObjectRelation.THIS), new ChooseOneAtTriggerTimeEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect(1)),
                new ChooseOneEffect.ChooseOneOption("You gain 3 life", new GainLifeEffect(3))))),
                new GainLifeEffect(1));

        assertThatThrownBy(() -> harness.enterBattlefieldAndReturn(player1, card))
                .hasStackTraceContaining("one ability is one stack object");
    }

    @Test
    @DisplayName("\"Whenever another creature leaves the battlefield, draw a card and you gain 1 life\" is one stack object")
    void anotherCreatureLeavesAbilityIsOneStackObject() {
        Card card = watcher("Leave Watcher");
        card.addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> removal().removePermanentToHand(gd, bears));

        assertThat(entriesOf("Leave Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Two \"whenever another creature leaves the battlefield\" abilities are two stack objects")
    void twoAnotherCreatureLeavesAbilitiesAreTwoStackObjects() {
        Card card = watcher("Leave Twin");
        card.addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new DrawCardEffect(1), new GainLifeEffect(1));
        card.addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER), new GainLifeEffect(2));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> removal().removePermanentToHand(gd, bears));

        assertThat(entriesOf("Leave Twin")).hasSize(2);
        assertThat(entriesOf("Leave Twin").stream().map(entry -> entry.getEffectsToResolve().size()))
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("\"Whenever another creature you control leaves the battlefield, destroy target creature and you gain 1 life\" chooses its target for the whole ability")
    void allyCreatureLeavesAbilityWithTargetIsOneStackObject() {
        Card card = watcher("Leave Avenger");
        card.addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new DestroyTargetPermanentEffect(), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent leaving = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player1.getId());

        harness.inMutationScope(() -> removal().removePermanentToHand(gd, leaving));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class)
                .processNextSpellTargetTrigger(gd));

        assertThat(entriesOf("Leave Avenger")).isEmpty();
        harness.handlePermanentChosen(player1, victim.getId());
        assertThat(entriesOf("Leave Avenger")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("\"Whenever a creature is returned to your hand from the battlefield, draw a card and you gain 1 life\" is one stack object")
    void creatureReturnedToHandAbilityIsOneStackObject() {
        Card card = watcher("Bounce Watcher");
        card.addTrigger(Triggers.putIntoHandFrom(Zone.BATTLEFIELD, Triggers.CREATURE, ObjectRelation.YOU_OWN),
                new DrawCardEffect(1), new GainLifeEffect(1));
        addCreatureReady(player1, card);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> removal().removePermanentToHand(gd, bears));

        assertThat(entriesOf("Bounce Watcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("Landfall from the graveyard, \"you may draw a card and you gain 1 life\", is one stack object")
    void graveyardLandfallAbilityIsOneStackObject() {
        Card card = watcher("Buried Landwatcher");
        card.addTrigger(new TriggerCondition(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"), new GainLifeEffect(1));
        harness.setGraveyard(player1, List.of(card));
        int lifeBefore = gd.getLife(player1.getId());

        harness.enterLandBattlefieldAndReturn(player1, new Forest());

        assertThat(entriesOf("Buried Landwatcher")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("\"When you sacrifice a permanent, you may draw a card and you gain 1 life\", from the graveyard, is one stack object")
    void graveyardSacrificeAbilityIsOneStackObject() {
        Card card = watcher("Buried Mourner");
        card.addTrigger(new TriggerCondition(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new MayEffect(new DrawCardEffect(1), "Draw a card?"), new GainLifeEffect(1));
        harness.setGraveyard(player1, List.of(card));
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.sacrifice(bears);

        assertThat(entriesOf("Buried Mourner")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }

    @Test
    @DisplayName("\"Whenever a creature you control enters, draw a card and you gain 1 life\", from the graveyard, is one stack object naming the creature")
    void graveyardCreatureEntersAbilityIsOneStackObject() {
        Card card = watcher("Buried Greeter");
        card.addTrigger(new TriggerCondition(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                        AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new DrawCardEffect(1), new GainLifeEffect(1));
        harness.setGraveyard(player1, List.of(card));

        Permanent bears = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(entriesOf("Buried Greeter")).singleElement().satisfies(entry -> {
            assertThat(entry.getEffectsToResolve()).hasSize(2);
            assertThat(entry.getTriggeringPermanentId()).isEqualTo(bears.getId());
            assertThat(entry.isNonTargeting()).isTrue();
        });
    }

    @Test
    @DisplayName("\"When this phases out, draw a card and you gain 1 life\" is one stack object")
    void phasesOutAbilityIsOneStackObject() {
        Card card = watcher("Phase Scholar");
        card.addTrigger(Triggers.phasesOut(null, ObjectRelation.THIS), new DrawCardEffect(1), new GainLifeEffect(1));
        Permanent scholar = addCreatureReady(player1, card);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(PhasingService.class)
                .phaseOut(gd, List.of(scholar)));

        assertThat(entriesOf("Phase Scholar")).singleElement()
                .satisfies(entry -> assertThat(entry.getEffectsToResolve()).hasSize(2));
    }
}
