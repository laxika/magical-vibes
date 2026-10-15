package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WickerfolkIndomitable.class, FountainOfYouth.class, GrizzlyBears.class, Swamp.class})
class WickerfolkIndomitableTest extends BaseCardTest {

    private void prepareGraveyardCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
    }

    @Test
    void canCastFromGraveyardBySacrificingAnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        harness.castFromGraveyardWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wickerfolk Indomitable");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canCastFromGraveyardBySacrificingACreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        harness.castFromGraveyardWithSacrifice(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wickerfolk Indomitable");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotCastWithoutASacrificeablePermanent() {
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(player1, 0, UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotOmitSacrificeEvenWithAnEligiblePermanent() {
        harness.addToBattlefield(player1, new WickerfolkIndomitable());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Wickerfolk Indomitable");
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotSacrificeALandThatIsNeitherArtifactNorCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Swamp");
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotSacrificeAnOpponentsPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WickerfolkIndomitable());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Wickerfolk Indomitable");
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void cannotCastWithLessThanTwoLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WickerfolkIndomitable());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void stillRequiresTheNormalManaCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WickerfolkIndomitable());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void graveyardPermissionDoesNotAllowCastingDuringCombat() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WickerfolkIndomitable());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void oneArtifactCreaturePaysTheEntireSacrificeCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WickerfolkIndomitable());
        harness.setGraveyard(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        harness.castFromGraveyardWithSacrifice(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Wickerfolk Indomitable");
    }

    @Test
    void castingFromHandDoesNotRequireLifeOrSacrifice() {
        harness.setHand(player1, List.of(new WickerfolkIndomitable()));
        prepareGraveyardCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wickerfolk Indomitable");
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }
}
