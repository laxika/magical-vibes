package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DroolingOgre;
import com.github.laxika.magicalvibes.cards.m.MyrMoonvessel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RitualOfRestoration.class, MyrMoonvessel.class, DroolingOgre.class})
class RitualOfRestorationTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target artifact card from your graveyard to your hand")
    void returnsTargetArtifactFromGraveyardToHand() {
        Card artifact = new MyrMoonvessel();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new RitualOfRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, artifact.getId());

        harness.assertInHand(player1, artifact.getName());
        harness.assertNotInGraveyard(player1, artifact.getName());
        harness.assertInGraveyard(player1, "Ritual of Restoration");
    }

    @Test
    @DisplayName("Cannot target a non-artifact card in your graveyard")
    void cannotTargetNonArtifactCard() {
        Card creature = new DroolingOgre();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RitualOfRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card artifact = new MyrMoonvessel();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new RitualOfRestoration()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
