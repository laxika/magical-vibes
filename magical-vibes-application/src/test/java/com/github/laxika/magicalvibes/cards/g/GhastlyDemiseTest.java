package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MassOfGhouls;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhastlyDemiseTest extends BaseCardTest {

    private void cast(List<Card> graveyard, UUID targetId) {
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys a nonblack creature whose toughness is at most the caster's graveyard size")
    void destroysCreatureWithinGraveyardThreshold() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        cast(List.of(new Forest(), new Forest()), targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target a creature above the threshold, but does not destroy it")
    void doesNothingAboveGraveyardThreshold() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        cast(List.of(new Forest()), targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counts only the caster's graveyard")
    void ignoresOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        cast(List.of(), targetId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player2, new MassOfGhouls());
        UUID targetId = harness.getPermanentId(player2, "Mass of Ghouls");
        harness.setHand(player1, List.of(new GhastlyDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
