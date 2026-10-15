package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TalasScout;
import com.github.laxika.magicalvibes.cards.z.ZanarkandAncientMetropolis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VaanStreetThief.class, TalasScout.class, Divination.class, Forest.class, GrizzlyBears.class})
class VaanStreetThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage exiles the damaged player's top card and declining creates a Treasure")
    void combatDamageDeclineCreatesTreasure() {
        addCreatureReady(player1, new VaanStreetThief());
        addAttackingScout();
        addAttackingScout();
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Accepting the may ability casts the exiled card and boosts the matching creatures")
    void combatDamageAcceptCastsAndBoosts() {
        Permanent vaan = addCreatureReady(player1, new VaanStreetThief());
        Permanent scout = addAttackingScout();
        Card topCard = new Divination();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombatAndTrigger();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(vaan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A land exiled by the trigger creates a Treasure without offering a cast")
    void landCreatesTreasure() {
        addCreatureReady(player1, new VaanStreetThief());
        addAttackingScout();
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage from a non-Scout, non-Pirate, and non-Rogue does not trigger")
    void nonMatchingCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new VaanStreetThief());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An empty damaged player's library still creates a Treasure")
    void emptyLibraryCreatesTreasure() {
        addCreatureReady(player1, new VaanStreetThief()).setAttacking(true);
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Accepting without enough mana leaves the card exiled and creates a Treasure")
    void cannotPayCreatesTreasure() {
        Permanent vaan = addCreatureReady(player1, new VaanStreetThief());
        vaan.setAttacking(true);
        Card topCard = new Divination();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(vaan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting an owned spell does not put counters on Scouts")
    void ownedSpellDoesNotBoost() {
        Permanent vaan = addCreatureReady(player1, new VaanStreetThief());
        Permanent scout = addCreatureReady(player1, new TalasScout());
        Card spell = new Divination();
        spell.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(vaan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An unowned spell boosts only matching permanents controlled by its caster")
    void unownedSpellBoostsOnlyControlledMatchingPermanents() {
        Permanent vaan = addCreatureReady(player1, new VaanStreetThief());
        Permanent scout = addCreatureReady(player1, new TalasScout());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingScout = addCreatureReady(player2, new TalasScout());
        Card spell = new Divination();
        spell.setOwnerId(player2.getId());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(vaan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(scout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingScout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({ZanarkandAncientMetropolis.class})
    @DisplayName("An exiled land with an Adventure offers its nonland Adventure for casting")
    void landAdventureCanBeCast() {
        addCreatureReady(player1, new VaanStreetThief()).setAttacking(true);
        Card topCard = new ZanarkandAncientMetropolis();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombat();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private Permanent addAttackingScout() {
        Permanent scout = addCreatureReady(player1, new TalasScout());
        scout.setAttacking(true);
        return scout;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
