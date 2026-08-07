package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExcavatorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Forest grants forestwalk to the target")
    void sacrificeForestGrantsForestwalk() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("The sacrificed land's type decides which landwalk is granted")
    void chosenLandDecidesLandwalk() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, islandId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.ISLANDWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature is a legal target")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Plains());
        Permanent enemyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, enemyBears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enemyBears, Keyword.PLAINSWALK)).isTrue();
    }

    @Test
    @DisplayName("Landwalk wears off at end of turn")
    void landwalkWearsOff() {
        harness.addToBattlefield(player1, new Excavator());
        harness.addToBattlefield(player1, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }

    @Test
    @DisplayName("Cannot be activated without a basic land to sacrifice")
    void requiresBasicLandToSacrifice() {
        harness.addToBattlefield(player1, new Excavator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
