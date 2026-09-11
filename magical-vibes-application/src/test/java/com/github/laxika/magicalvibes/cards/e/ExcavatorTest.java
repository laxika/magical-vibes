package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Excavator.class, Forest.class, Island.class, Mountain.class, Plains.class, Swamp.class,
        TrainedArmodon.class, Wasteland.class})
class ExcavatorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest grants forestwalk to the target")
    void sacrificeForestGrantsForestwalk() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("The sacrificed land's type decides which landwalk is granted")
    void chosenLandDecidesLandwalk() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.activateAbility(player1, 0, null, armodon.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature is a legal target")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Plains());
        Permanent enemyArmodon = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());

        harness.activateAbility(player1, 0, null, enemyArmodon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemyArmodon, Keyword.PLAINSWALK)).isTrue();
    }

    @Test
    @DisplayName("Landwalk wears off at end of turn")
    void landwalkWearsOff() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, armodon, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot be activated without a basic land to sacrifice")
    void requiresBasicLandToSacrifice() {
        harness.addToBattlefield(player1, new Excavator());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, armodon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Mountain grants mountainwalk")
    void sacrificeMountainGrantsMountainwalk() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Mountain());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, armodon, Keyword.MOUNTAINWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.SWAMPWALK)).isFalse();
    }

    @Test
    @DisplayName("A Swamp grants swampwalk")
    void sacrificeSwampGrantsSwampwalk() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Swamp());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, armodon, Keyword.SWAMPWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.MOUNTAINWALK)).isFalse();
    }

    @Test
    @DisplayName("A nonbasic land cannot be sacrificed for the ability")
    void cannotSacrificeNonbasicLand() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Wasteland());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, armodon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(UrborgTombOfYawgmoth.class)
    @DisplayName("Uses all effective land types of the sacrificed land")
    void usesEffectiveLandTypesOfSacrificedLand() {
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        harness.activateAbility(player1, 1, null, armodon.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, armodon, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.SWAMPWALK)).isTrue();
    }

    @Test
    @DisplayName("The ability still resolves after Excavator leaves the battlefield")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent excavator = harness.addToBattlefieldAndReturn(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        Permanent armodon = harness.addToBattlefieldAndReturn(player1, new TrainedArmodon());

        harness.activateAbility(player1, 0, null, armodon.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, excavator));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Excavator");
        assertThat(gqs.hasKeyword(gd, armodon, Keyword.FORESTWALK)).isTrue();
    }
}
