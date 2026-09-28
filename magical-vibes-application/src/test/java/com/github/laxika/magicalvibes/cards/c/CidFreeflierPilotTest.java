package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImperialRecoveryUnit;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CidFreeflierPilot.class, LeoninScimitar.class, ImperialRecoveryUnit.class, GrizzlyBears.class})
class CidFreeflierPilotTest extends BaseCardTest {

    @Test
    void equipmentAndVehicleSpellsCostOneLess() {
        harness.addToBattlefield(player1, new CidFreeflierPilot());

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ImperialRecoveryUnit()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void hasFlyingOnlyDuringItsControllersTurn() {
        Permanent cid = harness.addToBattlefieldAndReturn(player1, new CidFreeflierPilot());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, cid, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, cid, Keyword.FLYING)).isFalse();
    }

    @Test
    void returnsTargetEquipmentOrVehicleFromGraveyardToHand() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card equipment = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(equipment));

        harness.activateAbility(player1, 0, 0, null, equipment.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void cannotTargetOtherCardTypesInGraveyard() {
        addCreatureReady(player1, new CidFreeflierPilot());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
