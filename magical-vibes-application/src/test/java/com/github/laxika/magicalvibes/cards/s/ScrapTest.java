package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.c.CitanulFlute;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scrap.class, CitanulFlute.class, CoralMerfolk.class, Cathodion.class})
class ScrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CitanulFlute());
        harness.setHand(player1, List.of(new Scrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Citanul Flute");
        harness.assertInGraveyard(player2, "Citanul Flute");
    }

    @Test
    @DisplayName("Destroys an artifact creature")
    void destroysArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Cathodion());
        harness.setHand(player1, List.of(new Scrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, artifactCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cathodion");
        harness.assertInGraveyard(player2, "Cathodion");
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifactPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CoralMerfolk());

        harness.setHand(player1, List.of(new Scrap()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Scrap and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Scrap()));
        harness.setLibrary(player1, List.of(new CoralMerfolk()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scrap");
        harness.assertInHand(player1, "Coral Merfolk");
    }
}
