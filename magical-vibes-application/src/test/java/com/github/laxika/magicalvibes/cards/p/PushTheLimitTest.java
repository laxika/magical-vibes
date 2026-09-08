package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BrightfieldMustang;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PushTheLimitTest extends BaseCardTest {

    @Test
    @DisplayName("Returns Mounts and Vehicles and sacrifices them at the next end step")
    void returnsMountsAndVehiclesAndSacrificesThemAtNextEndStep() {
        harness.setGraveyard(player1, List.of(new BrightfieldMustang(), new DuskLegionDreadnought(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brightfield Mustang");
        harness.assertOnBattlefield(player1, "Dusk Legion Dreadnought");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brightfield Mustang");
        harness.assertInGraveyard(player1, "Dusk Legion Dreadnought");
        harness.assertNotOnBattlefield(player1, "Brightfield Mustang");
        harness.assertNotOnBattlefield(player1, "Dusk Legion Dreadnought");
    }

    @Test
    @DisplayName("Makes own Vehicles creatures and gives own creatures haste until end of turn")
    void animatesOwnVehiclesAndGivesOwnCreaturesHasteUntilEndOfTurn() {
        Permanent ownVehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PushTheLimit()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentVehicle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownVehicle, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isFalse();
    }
}
