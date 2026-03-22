package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YawgmothsVileOfferingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has correct effects: reanimate, destroy, exile self")
    void hasCorrectEffects() {
        YawgmothsVileOffering card = new YawgmothsVileOffering();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(ReturnCardFromGraveyardEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(ExileSpellEffect.class);
    }

    // ===== Legendary sorcery restriction =====

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can cast when controlling a legendary creature")
    void canCastWithLegendaryCreature() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId(), List.of());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    // ===== Both targets: reanimate + destroy =====

    @Test
    @DisplayName("Reanimates creature from graveyard and destroys target creature")
    void reanimatesAndDestroys() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(opponentCreatureId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Graveyard creature reanimated under our control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()));

        // Opponent's creature destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Spell exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
    }

    // ===== Reanimate from opponent's graveyard =====

    @Test
    @DisplayName("Can reanimate a creature from opponent's graveyard")
    void reanimatesFromOpponentGraveyard() {
        Card opponentCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, opponentCreature.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature enters under our control (not opponent's)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(opponentCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(opponentCreature.getId()));
    }

    // ===== Graveyard only (no destroy target) =====

    @Test
    @DisplayName("Can cast with only a graveyard target and no permanent target")
    void canCastWithOnlyGraveyardTarget() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature reanimated
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));

        // Spell exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
    }

    // ===== Destroy only (no graveyard target) =====

    @Test
    @DisplayName("Can cast with only a permanent target and no graveyard target")
    void canCastWithOnlyPermanentTarget() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, List.of(opponentCreatureId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Opponent's creature destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Spell exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
    }

    // ===== Cannot target non-creature/planeswalker =====

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureInGraveyard() {
        Card instant = new HolyDay();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Exile self =====

    @Test
    @DisplayName("Is exiled after resolution instead of going to graveyard")
    void isExiledAfterResolution() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId(), List.of());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Graveyard target removed — destroy still resolves")
    void graveyardTargetRemovedDestroyStillResolves() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, creature.getId(), List.of(opponentCreatureId));

        // Remove graveyard card before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Graveyard target fizzled — nothing reanimated
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));

        // Destroy still resolves
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Permanent target removed — reanimate still resolves")
    void permanentTargetRemovedReanimateStillResolves() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castSorcery(player1, 0, creature.getId(), List.of(opponentCreatureId));

        // Remove permanent before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Reanimate still resolves
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
    }
}
