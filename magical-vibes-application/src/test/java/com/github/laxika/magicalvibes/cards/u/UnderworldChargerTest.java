package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HavengulLich;
import com.github.laxika.magicalvibes.cards.n.NyxbornMarauder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderworldCharger.class, NyxbornMarauder.class, HavengulLich.class})
class UnderworldChargerTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters without +1/+1 counters")
    void castFromHandHasNoCounters() {
        harness.setHand(player1, List.of(new UnderworldCharger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent charger = findPermanent(player1, "Underworld Charger");
        assertThat(charger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(charger.getEffectivePower()).isEqualTo(3);
        assertThat(charger.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Escape exiles three other graveyard cards and enters with two +1/+1 counters")
    void escapeExilesOtherCardsAndEntersWithCounters() {
        UnderworldCharger charger = new UnderworldCharger();
        harness.setGraveyard(player1, List.of(charger, new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        harness.passBothPriorities();

        Permanent escapedCharger = findPermanent(player1, "Underworld Charger");
        assertThat(escapedCharger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(escapedCharger.getEffectivePower()).isEqualTo(5);
        assertThat(escapedCharger.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Escape requires three other cards in the graveyard")
    void escapeRequiresThreeOtherCards() {
        UnderworldCharger charger = new UnderworldCharger();
        harness.setGraveyard(player1, List.of(charger, new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Underworld Charger");
    }

    @Test
    @DisplayName("Underworld Charger cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        Permanent charger = addCreatureReady(player2, new UnderworldCharger());
        Permanent attacker = addCreatureReady(player1, new NyxbornMarauder());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(charger), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Using the printed escape ability makes the permanent escaped")
    void printedEscapeMarksPermanentAsEscaped() {
        harness.setGraveyard(player1, List.of(new UnderworldCharger(),
                new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Underworld Charger").isEscaped()).isTrue();
    }

    @Test
    @DisplayName("Casting with Havengul Lich's permission does not grant escape counters")
    void nonEscapeGraveyardCastHasNoCounters() {
        harness.addToBattlefield(player1, new HavengulLich());
        UnderworldCharger charger = new UnderworldCharger();
        harness.setGraveyard(player1, List.of(charger));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, charger.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, charger.getId());
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Underworld Charger");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(permanent.isEscaped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Escape cannot exile Underworld Charger itself even with enough other cards")
    void escapeCannotExileItself() {
        harness.setGraveyard(player1, List.of(new UnderworldCharger(),
                new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Escape cannot count the same graveyard card more than once")
    void escapeRequiresDistinctExiledCards() {
        harness.setGraveyard(player1, List.of(new UnderworldCharger(),
                new NyxbornMarauder(), new NyxbornMarauder(), new NyxbornMarauder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
