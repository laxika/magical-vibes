package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CampusGuide;
import com.github.laxika.magicalvibes.cards.h.HeatedDebate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TendThePests.class, CampusGuide.class, HeatedDebate.class})
class TendThePestsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Pest for each power of the sacrificed creature")
    void createsPestsEqualToSacrificedPower() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castTendThePests(sacrifice);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .hasSize(3);
        harness.assertInGraveyard(player1, "Campus Guide");
    }

    @Test
    @DisplayName("Pest tokens gain life when they die")
    void pestDeathGainsLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        castTendThePests(sacrifice);

        Permanent pest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .findFirst()
                .orElseThrow();
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new HeatedDebate()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, pest.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        harness.setHand(player1, List.of(new TendThePests()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void zeroPowerCreatesNoPests() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        sacrifice.setPersistentPowerModifier(-2);

        castTendThePests(sacrifice);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Campus Guide");
    }

    @Test
    void negativePowerCreatesNoPests() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        sacrifice.setPersistentPowerModifier(-3);

        castTendThePests(sacrifice);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Campus Guide");
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CampusGuide());
        harness.setHand(player1, List.of(new TendThePests()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(
                player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    void sacrificeIsPaidBeforeTokensAreCreated() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CampusGuide());
        harness.setHand(player1, List.of(new TendThePests()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());

        harness.assertInGraveyard(player1, "Campus Guide");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.PEST))
                .hasSize(2);
    }
    private void castTendThePests(Permanent sacrifice) {
        harness.setHand(player1, List.of(new TendThePests()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstantWithSacrifice(player1, 0, null, sacrifice.getId());
        harness.passBothPriorities();
    }
}

