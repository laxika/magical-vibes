package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChandraAcolyteOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("0 puts loyalty counters on red planeswalkers you control")
    void zeroPutsCountersOnControlledRedPlaneswalkers() {
        Permanent chandra = addReadyChandra(player1, 3);
        Permanent otherRedPlaneswalker = addPlaneswalker(player1, new ChandraNalaar(), 3);
        Permanent bluePlaneswalker = addPlaneswalker(player1, new JaceBeleren(), 3);
        Permanent opposingRedPlaneswalker = addPlaneswalker(player2, new ChandraNalaar(), 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(otherRedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(bluePlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(opposingRedPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("0 creates hasty Elementals that are sacrificed at the next end step")
    void zeroCreatesHastyElementalsUntilNextEndStep() {
        addReadyChandra(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).allSatisfy(elemental -> {
            assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(elemental.getEffectivePower()).isEqualTo(1);
            assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, elemental, Keyword.HASTE)).isTrue();
        });

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    @DisplayName("-2 casts a qualifying graveyard spell for its mana cost and exiles it afterward")
    void minusTwoCastsQualifyingGraveyardSpell() {
        addReadyChandra(player1, 5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 2, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = new Permanent(new ChandraAcolyteOfFlame());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(chandra);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return chandra;
    }

    private Permanent addPlaneswalker(Player player, Card card, int loyalty) {
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
