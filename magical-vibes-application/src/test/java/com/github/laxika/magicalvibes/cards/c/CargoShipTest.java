package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.i.ItemShopkeep;
import com.github.laxika.magicalvibes.cards.p.PhoenixDown;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CargoShip.class, ItemShopkeep.class, PhoenixDown.class})
class CargoShipTest extends BaseCardTest {

    @Test
    void addsArtifactOnlyColorlessMana() {
        Permanent ship = addShipReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(1);
        assertThat(ship.isTapped()).isTrue();
    }

    @Test
    void artifactOnlyManaCanCastArtifactSpell() {
        addShipReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new CargoShip()));
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    void artifactOnlyManaCannotCastNonartifactSpell() {
        addShipReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new ItemShopkeep()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void crewAnimatesShipAndTapsCrew() {
        Permanent ship = addShipReady(player1);
        Permanent crew = addCreatureReady(player1, new ItemShopkeep());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(ship.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, ship)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void restrictedManaCanActivateAnArtifactAbility() {
        addShipReady(player1);
        harness.addToBattlefield(player1, new PhoenixDown());
        ItemShopkeep creature = new ItemShopkeep();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 1, 0, 0, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
        assertThat(findPermanent(player1, "Item Shopkeep").isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Phoenix Down");
    }

    @Test
    void uncrewedShipCanProduceManaTheTurnItEnters() {
        Permanent ship = harness.addToBattlefieldAndReturn(player1, new CargoShip());
        ship.setSummoningSick(true);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(ship.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newlyEnteredCreatureCanCrewButNewlyEnteredCrewedShipCannotTapForMana() {
        Permanent ship = harness.addToBattlefieldAndReturn(player1, new CargoShip());
        ship.setSummoningSick(true);
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new ItemShopkeep());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, ship)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ship)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(ship.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyColorless()).isZero();
    }

    @Test
    void tappedCreaturesAndOpponentsCreaturesCannotPayCrewCost() {
        Permanent ship = addShipReady(player1);
        Permanent tappedCrew = addCreatureReady(player1, new ItemShopkeep());
        tappedCrew.tap();
        addCreatureReady(player2, new ItemShopkeep());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(gqs.isCreature(gd, ship)).isFalse();
    }

    @Test
    void tappedShipCanBeCrewedAndAnimationExpiresAtEndOfTurn() {
        Permanent ship = addShipReady(player1);
        addCreatureReady(player1, new ItemShopkeep());
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ship)).isTrue();
        assertThat(ship.isTapped()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ship)).isFalse();
    }

    private Permanent addShipReady(Player player) {
        return addCreatureReady(player, new CargoShip());
    }

    @Test
    void crewedShipAttacksWithoutTappingAndCannotBeBlockedByAGroundCreature() {
        Permanent ship = addShipReady(player1);
        addCreatureReady(player1, new ItemShopkeep());
        addCreatureReady(player2, new ItemShopkeep());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(ship.isTapped()).isFalse();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
