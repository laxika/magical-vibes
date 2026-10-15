package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.k.KarnScionOfUrza;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YawgmothsVileOffering.class, ArvadTheCursed.class, PrimordialWurm.class,
        Divination.class, KarnScionOfUrza.class})
class YawgmothsVileOfferingTest extends BaseCardTest {

    // ===== Legendary sorcery restriction =====

    @Test
    @DisplayName("Cannot cast without controlling a legendary creature or planeswalker")
    void cannotCastWithoutLegendaryPermanent() {
        Card creature = new PrimordialWurm();
        harness.addToBattlefield(player1, new PrimordialWurm());
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
        Card creature = new PrimordialWurm();
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
        Card graveyardCreature = new PrimordialWurm();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Primordial Wurm");

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(opponentCreatureId));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Graveyard creature reanimated under our control
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()));

        // Opponent's creature destroyed
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        harness.assertInGraveyard(player2, "Primordial Wurm");

        // Spell exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
        harness.assertNotInGraveyard(player1, "Yawgmoth's Vile Offering");
    }

    // ===== Reanimate from opponent's graveyard =====

    @Test
    @DisplayName("Can reanimate a creature from opponent's graveyard")
    void reanimatesFromOpponentGraveyard() {
        Card opponentCreature = new PrimordialWurm();
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
        Card creature = new PrimordialWurm();
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
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Primordial Wurm");

        harness.castAndResolveSorcery(player1, 0, List.of(opponentCreatureId));

        GameData gd = harness.getGameData();

        // Opponent's creature destroyed
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");

        // Spell exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
    }

    // ===== Cannot target non-creature/planeswalker =====

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureInGraveyard() {
        Card nonCreature = new Divination();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Exile self =====

    @Test
    @DisplayName("Is exiled after resolution instead of going to graveyard")
    void isExiledAfterResolution() {
        Card creature = new PrimordialWurm();
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
        harness.assertNotInGraveyard(player1, "Yawgmoth's Vile Offering");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Yawgmoth's Vile Offering"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Graveyard target removed — destroy still resolves")
    void graveyardTargetRemovedDestroyStillResolves() {
        Card creature = new PrimordialWurm();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Primordial Wurm");

        harness.castSorcery(player1, 0, creature.getId(), List.of(opponentCreatureId));

        // Remove graveyard card before resolution
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Graveyard target fizzled — nothing reanimated
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));

        // Destroy still resolves
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
    }

    @Test
    @DisplayName("Permanent target removed — reanimate still resolves")
    void permanentTargetRemovedReanimateStillResolves() {
        Card creature = new PrimordialWurm();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Primordial Wurm");

        harness.castSorcery(player1, 0, creature.getId(), List.of(opponentCreatureId));

        // Remove permanent before resolution
        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Primordial Wurm"));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Reanimate still resolves
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Can choose zero targets and still exile the spell")
    void resolvesWithZeroTargets() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, List.of());
        assertThat(gd.stack).isEmpty();
        harness.assertNotInGraveyard(player1, "Yawgmoth's Vile Offering");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof YawgmothsVileOffering);
    }

    @Test
    @DisplayName("Reanimates an opposing planeswalker and destroys another planeswalker")
    void reanimatesAndDestroysPlaneswalkers() {
        Card graveyardWalker = new KarnScionOfUrza();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new KarnScionOfUrza());
        harness.setGraveyard(player2, List.of(graveyardWalker));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        UUID target = harness.getPermanentId(player2, "Karn, Scion of Urza");
        harness.castSorcery(player1, 0, graveyardWalker.getId(), List.of(target));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Karn, Scion of Urza");
        harness.assertNotOnBattlefield(player2, "Karn, Scion of Urza");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(graveyardWalker.getId()));
        harness.assertInGraveyard(player2, "Karn, Scion of Urza");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof YawgmothsVileOffering);
    }

    @Test
    @DisplayName("A legendary planeswalker satisfies the casting restriction")
    void canCastWithLegendaryPlaneswalker() {
        harness.addToBattlefield(player1, new KarnScionOfUrza());
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, List.of());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof YawgmothsVileOffering);
    }

    @Test
    @DisplayName("When every chosen target becomes illegal the spell goes to the graveyard")
    void allChosenTargetsIllegalDoesNotExileSpell() {
        Card creature = new PrimordialWurm();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.addToBattlefield(player2, new PrimordialWurm());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        UUID target = harness.getPermanentId(player2, "Primordial Wurm");
        harness.castSorcery(player1, 0, creature.getId(), List.of(target));
        harness.setGraveyard(player1, List.of());
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Yawgmoth's Vile Offering");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c instanceof YawgmothsVileOffering);
        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
    }

    @Test
    @DisplayName("Losing the legendary creature after casting does not prevent resolution")
    void losingLegendaryCreatureAfterCastingStillResolves() {
        Card creature = new PrimordialWurm();
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new YawgmothsVileOffering()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, creature.getId(), List.of());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c instanceof YawgmothsVileOffering);
    }
}
