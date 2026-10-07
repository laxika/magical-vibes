package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.c.ChandraAwakenedInferno;
import com.github.laxika.magicalvibes.cards.j.JhoiraWeatherlightCaptain;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TalesEnd.class, AdelizTheCinderWind.class, ChandraAwakenedInferno.class, JhoiraWeatherlightCaptain.class,
        RodOfRuin.class, Shock.class, ShivanDragon.class, Spellbook.class})
class TalesEndTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target activated ability")
    void countersActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new TalesEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, rod.getId());

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a target triggered ability")
    void countersTriggeredAbility() {
        harness.addToBattlefield(player2, new JhoiraWeatherlightCaptain());
        harness.setHand(player2, List.of(new Spellbook()));
        harness.setHand(player1, List.of(new TalesEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castArtifact(player2, 0);
        StackEntry trigger = harness.getGameData().stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow();
        harness.passPriority(player2);

        int handSizeBefore = harness.getGameData().playerHands.get(player2.getId()).size();
        harness.castAndResolveInstant(player1, 0, trigger.getCard().getId());

        assertThat(harness.getGameData().stack).noneMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(harness.getGameData().playerHands.get(player2.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Counters a target legendary spell")
    void countersLegendarySpell() {
        AdelizTheCinderWind adeliz = new AdelizTheCinderWind();
        harness.setHand(player2, List.of(adeliz));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new TalesEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, adeliz.getId());

        harness.assertInGraveyard(player2, "Adeliz, the Cinder Wind");
        harness.assertNotOnBattlefield(player2, "Adeliz, the Cinder Wind");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary spell")
    void cannotTargetNonlegendarySpell() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new TalesEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter one of two abilities from the same source, including your own ability")
    void countersOnlyChosenAbilityFromSameSource() {
        harness.addToBattlefield(player1, new ShivanDragon());
        harness.setHand(player1, List.of(new TalesEnd()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        StackEntry firstAbility = harness.getGameData().stack.getLast();
        harness.activateAbility(player1, 0, null, null);
        StackEntry secondAbility = harness.getGameData().stack.getLast();

        harness.castAndResolveInstant(player1, 0, firstAbility.getTargetableId());

        assertThat(harness.getGameData().stack).containsExactly(secondAbility);
        harness.assertOnBattlefield(player1, "Shivan Dragon");
        harness.assertNotInGraveyard(player1, "Shivan Dragon");
        harness.assertInGraveyard(player1, "Tale's End");

        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst()
                .getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("An uncounterable legendary spell is a legal target but is not countered")
    void cannotCounterUncounterableLegendarySpell() {
        ChandraAwakenedInferno chandra = new ChandraAwakenedInferno();
        harness.setHand(player2, List.of(chandra));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new TalesEnd()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castPlaneswalker(player2, 0);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, chandra.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getTargetableId()).isEqualTo(chandra.getId());
        harness.assertNotInGraveyard(player2, "Chandra, Awakened Inferno");
        harness.assertInGraveyard(player1, "Tale's End");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Chandra, Awakened Inferno");
        harness.assertNotInGraveyard(player2, "Chandra, Awakened Inferno");
    }
}
