package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheVision.class, Spellbook.class, GrizzlyBears.class})
class TheVisionTest extends BaseCardTest {

    private static final String SOLAR_BEAM =
            "Solar Beam — The Vision gains double strike until end of turn.";
    private static final String DENSITY_CONTROL =
            "Density Control — The Vision gains indestructible until end of turn.";
    private static final String TECHNOPATHY = "Technopathy — Draw a card.";

    @Test
    @DisplayName("Each mode is available once for noncreature spells in a turn")
    void eachModeIsAvailableOncePerTurn() {
        var vision = addCreatureReady(player1, new TheVision());
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook(), new Spellbook(), new Spellbook()));
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));

        castArtifactAndChoose(SOLAR_BEAM);
        assertThat(gqs.hasKeyword(gd, vision, Keyword.DOUBLE_STRIKE)).isTrue();

        castArtifactAndChoose(DENSITY_CONTROL);
        assertThat(gqs.hasKeyword(gd, vision, Keyword.INDESTRUCTIBLE)).isTrue();

        castArtifactAndChoose(TECHNOPATHY);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);

        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("Creature spells do not trigger the modal ability")
    void creatureSpellsDoNotTrigger() {
        addCreatureReady(player1, new TheVision());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    private void castArtifactAndChoose(String mode) {
        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
