package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FutureFlight;
import com.github.laxika.magicalvibes.cards.a.AmateurHero;
import com.github.laxika.magicalvibes.cards.g.GrendelSpawnOfKnull;
import com.github.laxika.magicalvibes.cards.t.Tarmogoyf;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomDeadlyDevourer.class, GrendelSpawnOfKnull.class, AmateurHero.class,
        FutureFlight.class, Tarmogoyf.class})
class VenomDeadlyDevourerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature, then puts counters equal to its toughness on a Symbiote")
    void exilesThenCountersSymbioteUsingToughness() {
        addCreatureReady(player1, new VenomDeadlyDevourer());
        Permanent symbiote = addCreatureReady(player1, new GrendelSpawnOfKnull());
        Card graveyardCreature = new AmateurHero();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCreature);
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, symbiote.getId());
        harness.passBothPriorities();

        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Requires a creature card as the graveyard target")
    void requiresCreatureCardTarget() {
        addCreatureReady(player1, new VenomDeadlyDevourer());
        Card noncreature = new FutureFlight();
        harness.setGraveyard(player2, List.of(noncreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not create counters when no Symbiote is available")
    void doesNothingWithoutSymbioteTarget() {
        addCreatureReady(player1, new VenomDeadlyDevourer());
        Card graveyardCreature = new AmateurHero();
        harness.setGraveyard(player2, List.of(graveyardCreature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, graveyardCreature.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(graveyardCreature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnOpponentsSymbioteAndOwnGraveyard() {
        Permanent venom = addCreatureReady(player1, new VenomDeadlyDevourer());
        Permanent symbiote = addCreatureReady(player2, new GrendelSpawnOfKnull());
        Card creature = new AmateurHero();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, symbiote.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(symbiote.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void missingGraveyardTargetDoesNotCreateReflexiveTrigger() {
        Permanent venom = addCreatureReady(player1, new VenomDeadlyDevourer());
        Card creature = new AmateurHero();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void usesCharacteristicDefinedToughnessInExile() {
        Permanent venom = addCreatureReady(player1, new VenomDeadlyDevourer());
        Card creature = new Tarmogoyf();
        harness.setGraveyard(player2, List.of(creature, new AmateurHero(), new FutureFlight()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, venom.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void determinesExiledCardsToughnessWhenReflexiveTriggerResolves() {
        Permanent venom = addCreatureReady(player1, new VenomDeadlyDevourer());
        Card creature = new Tarmogoyf();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, venom.getId());
        harness.setGraveyard(player2, List.of(new AmateurHero(), new FutureFlight()));
        harness.passBothPriorities();

        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void reflexiveTriggerOnlyOffersSymbiotesAndCanTargetVenomItself() {
        Permanent venom = addCreatureReady(player1, new VenomDeadlyDevourer());
        Permanent hero = addCreatureReady(player1, new AmateurHero());
        Card creature = new AmateurHero();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        var choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(venom.getId()).doesNotContain(hero.getId());

        harness.handlePermanentChosen(player1, venom.getId());
        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(venom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(hero.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
