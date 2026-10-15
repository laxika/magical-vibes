package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GeomancersGambit;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OnThinIce.class, SnowCoveredPlains.class, Plains.class, GrizzlyBears.class,
        Naturalize.class, GeomancersGambit.class})
class OnThinIceTest extends BaseCardTest {

    private void castAndResolve(Permanent land, Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, List.of(land.getId(), creature.getId()));
        resolveAllTriggers();
    }

    @Test
    @DisplayName("ETB exiles target creature an opponent controls")
    void etbExilesOpponentCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(land, creature);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled creature returns when On Thin Ice leaves the battlefield")
    void exiledCreatureReturnsWhenAuraLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(land, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "On Thin Ice"));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot enchant a nonsnow land")
    void cannotEnchantNonsnowLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant an opponent's snow land")
    void cannotEnchantOpponentsSnowLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new SnowCoveredPlains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a creature controlled by the caster")
    void cannotExileOwnCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("A creature is not exiled if the Aura leaves before its ETB resolves")
    void auraLeavesBeforeExileResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "On Thin Ice");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "On Thin Ice"));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "On Thin Ice");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The Aura can be cast when no opponent controls a creature")
    void canEnchantLandWithoutOpponentCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        harness.setHand(player1, List.of(new OnThinIce()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, land.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "On Thin Ice").getAttachedTo()).isEqualTo(land.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exiled creature returns when the enchanted land is destroyed")
    void creatureReturnsWhenEnchantedLandLeaves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castAndResolve(land, creature);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GeomancersGambit()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, land.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Snow-Covered Plains");
        harness.assertInGraveyard(player1, "On Thin Ice");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
