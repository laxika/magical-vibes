package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RootwaterDiverTest extends BaseCardTest {

    @Test
    @DisplayName("Returns targeted artifact card from graveyard to hand and sacrifices itself")
    void returnsArtifactFromGraveyardToHand() {
        addCreatureReady(player1, new RootwaterDiver());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player1, List.of(feather));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, feather.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Rootwater Diver");
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(feather.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(feather.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonartifact card in the graveyard")
    void cannotTargetNonArtifact() {
        addCreatureReady(player1, new RootwaterDiver());
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
