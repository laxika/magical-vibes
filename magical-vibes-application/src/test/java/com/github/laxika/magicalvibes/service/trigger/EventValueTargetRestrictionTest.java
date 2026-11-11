package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.cards.b.BorosReckoner;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighMarket;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KrileBaldesion;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RiveteersAscendancy;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VenerableWarsinger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A triggered ability whose target is restricted by the event it triggered on, or whose effect reads "that much",
 * reads the event: the target is chosen against the record of the event as the ability goes on the stack (CR 603.3d)
 * and judged against the stack entry's as it resolves. The entry's X stays the X an ability that chooses nothing
 * has, which for these abilities is none.
 */
@CardUsed({BorosReckoner.class, VenerableWarsinger.class, RiveteersAscendancy.class, KrileBaldesion.class,
        HighMarket.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class, Shock.class})
class EventValueTargetRestrictionTest extends BaseCardTest {

    private StackEntry entryOf(String name) {
        List<StackEntry> entries = gd.stack.stream()
                .filter(entry -> entry.getCard() != null && name.equals(entry.getCard().getName()))
                .toList();
        assertThat(entries).as("stack entries of %s", name).hasSize(1);
        return entries.getFirst();
    }

    private List<UUID> offeredGraveyardCards() {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).as("a graveyard target is being chosen").isNotNull();
        return choice.validCardIds();
    }

    @Test
    @DisplayName("\"That much damage\" is the event's value, and the targeted ability has no X")
    void thatMuchDamageIsTheEventValue() {
        harness.addToBattlefield(player2, new BorosReckoner());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Boros Reckoner"));
        harness.handlePermanentChosen(player2, player1.getId());

        StackEntry ability = entryOf("Boros Reckoner");
        assertThat(ability.getXValue()).isZero();
        assertThat(ability.getEventValue()).isEqualTo(2);

        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("\"Mana value X or less, where X is the damage dealt\" is chosen and judged against the damage")
    void manaValueAtMostTheDamageDealt() {
        Card legal = new GrizzlyBears();
        Card tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(legal, tooExpensive));
        Permanent warsinger = addCreatureReady(player1, new VenerableWarsinger());
        warsinger.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(offeredGraveyardCards()).containsExactly(legal.getId());
        harness.handleMultipleCardsChosen(player1, List.of(legal.getId()));

        StackEntry ability = entryOf("Venerable Warsinger");
        assertThat(ability.getXValue()).isZero();
        assertThat(ability.getEventValue()).isEqualTo(3);

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(legal.getId()));
    }

    @Test
    @DisplayName("\"Lesser mana value\" is chosen and judged against the sacrificed creature")
    void lesserManaValueThanTheSacrificedCreature() {
        harness.addToBattlefield(player1, new RiveteersAscendancy());
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Card lesser = new GrizzlyBears();
        Card equal = new HillGiant();
        harness.setGraveyard(player1, List.of(lesser, equal));

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(offeredGraveyardCards()).containsExactly(lesser.getId());
        harness.handleMultipleCardsChosen(player1, List.of(lesser.getId()));

        StackEntry ability = entryOf("Riveteers Ascendancy");
        assertThat(ability.getXValue()).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == lesser);
    }

    @Test
    @DisplayName("\"Mana value equal to that spell's mana value\" is chosen and judged against the spell cast")
    void manaValueEqualToTheSpellCast() {
        harness.addToBattlefield(player1, new KrileBaldesion());
        Card equal = new LlanowarElves();
        Card greater = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(greater, equal));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(offeredGraveyardCards()).containsExactly(equal.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equal.getId()));

        StackEntry ability = entryOf("Krile Baldesion");
        assertThat(ability.getXValue()).isZero();
        assertThat(ability.getEventValue()).isEqualTo(1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Llanowar Elves");
    }
}
