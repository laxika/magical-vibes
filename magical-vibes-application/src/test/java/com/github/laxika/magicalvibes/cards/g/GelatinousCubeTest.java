package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BiogenicOoze;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GelatinousCube.class, GrizzlyBears.class, BiogenicOoze.class, Unsummon.class})
class GelatinousCubeTest extends BaseCardTest {

    @Test
    @DisplayName("Engulf exiles an opponent's non-Ooze creature until Gelatinous Cube leaves")
    void engulfsUntilCubeLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent cube = castCube(bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(cube.getId()))
                .extracting(Card::getId).containsExactly(bears.getCard().getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, cube.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dissolve puts the targeted exiled creature card into its owner's graveyard on resolution")
    void dissolvePutsExiledCreatureIntoOwnersGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent cube = castCube(bears.getId());
        UUID exiledCardId = gd.getCardsExiledByPermanent(cube.getId()).getFirst().getId();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cube),
                2, exiledCardId, Zone.EXILE);

        assertThat(gd.findExiledCard(exiledCardId)).isNotNull();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCardId)).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dissolve requires the target creature card's mana value to equal X")
    void dissolveRejectsWrongManaValue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent cube = castCube(bears.getId());
        UUID exiledCardId = gd.getCardsExiledByPermanent(cube.getId()).getFirst().getId();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(cube), 1, exiledCardId, Zone.EXILE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana value");
        assertThat(gd.findExiledCard(exiledCardId)).isNotNull();
    }

    @Test
    @DisplayName("Engulf cannot target an Ooze")
    void cannotEngulfOoze() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player2, new BiogenicOoze());
        harness.setHand(player1, List.of(new GelatinousCube()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, ooze.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Engulf does nothing if the Cube leaves before the trigger resolves")
    void sourceLeavesBeforeEngulfResolves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GelatinousCube()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        Permanent cube = findPermanent(player1, "Gelatinous Cube");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, cube.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Gelatinous Cube");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
    }

    @Test
    @DisplayName("Engulf cannot target a creature controlled by the Cube's controller")
    void cannotEngulfOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GelatinousCube()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A dissolved creature does not return when the Cube leaves")
    void dissolvedCreatureDoesNotReturn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent cube = castCube(bears.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cube),
                2, bears.getCard().getId(), Zone.EXILE);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, cube.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dissolve cannot move a creature returned by the Cube leaving in response")
    void sourceLeavesInResponseToDissolve() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent cube = castCube(bears.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cube),
                2, bears.getCard().getId(), Zone.EXILE);

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, cube.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dissolve cannot target a creature exiled by another Cube")
    void dissolveRejectsCardExiledByAnotherCube() {
        Permanent firstBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstCube = castCube(firstBears.getId());
        castCube(secondBears.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(firstCube),
                2, secondBears.getCard().getId(), Zone.EXILE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not exiled with");
        assertThat(gd.findExiledCard(secondBears.getCard().getId())).isNotNull();
    }

    private Permanent castCube(UUID targetId) {
        harness.setHand(player1, List.of(new GelatinousCube()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Gelatinous Cube");
    }
}
