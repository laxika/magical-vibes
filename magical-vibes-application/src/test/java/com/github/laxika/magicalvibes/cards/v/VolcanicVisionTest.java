package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicVision.class, HolyDay.class, LavaAxe.class, GrizzlyBears.class, Forest.class})
class VolcanicVisionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an instant and deals its mana value to each opponent creature")
    void returnsInstantAndDamagesOpponentCreatures() {
        Card instant = new HolyDay();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new VolcanicVision()));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, instant.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(instant.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volcanic Vision"));
    }

    @Test
    @DisplayName("Returns a sorcery and deals its mana value to opponent creatures")
    void returnsSorceryAndDealsItsManaValue() {
        Card sorcery = new LavaAxe();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new VolcanicVision()));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, sorcery.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(sorcery.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volcanic Vision"));
    }

    @Test
    @DisplayName("Cannot target a creature card in a graveyard")
    void cannotTargetCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VolcanicVision()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land card in a graveyard")
    void cannotTargetLandCard() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.setHand(player1, List.of(new VolcanicVision()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
