package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.Blaze;
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

@CardUsed({VolcanicVision.class, HolyDay.class, LavaAxe.class, GrizzlyBears.class, Forest.class, Blaze.class})
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

        harness.castAndResolveSorcery(player1, 0, instant.getId());

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

        harness.castAndResolveSorcery(player1, 0, sorcery.getId());

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

    @Test
    @DisplayName("Cannot target an instant in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new VolcanicVision()));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal graveyard target prevents damage and self-exile")
    void removedTargetPreventsAllEffects() {
        Card sorcery = new LavaAxe();
        Card vision = new VolcanicVision();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(vision));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, sorcery.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(sorcery));
        harness.passBothPriorities();

        assertThat(opponentCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Lava Axe");
        harness.assertInGraveyard(player1, "Volcanic Vision");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(vision.getId()));
    }

    @Test
    @DisplayName("X is zero in the returned card's mana value and all opposing creatures take damage")
    void returnedXSpellDamagesEveryOpponentCreature() {
        Card sorcery = new Blaze();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent firstOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new VolcanicVision()));
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, sorcery.getId());

        harness.assertInHand(player1, "Blaze");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(firstOpponentCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(secondOpponentCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotInGraveyard(player1, "Volcanic Vision");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Volcanic Vision"));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
