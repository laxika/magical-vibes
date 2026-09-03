package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagicalHack.class, GrizzlyBears.class, BogWraith.class})
class MagicalHackTest extends BaseCardTest {

    @Test
    @DisplayName("May target a permanent with no matching basic land type")
    void canTargetPermanentWithoutMatchingLandType() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player2, "Grizzly Bears").getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Plains"));
    }

    @Test
    @DisplayName("Changes a basic land type in a target permanent landwalk ability")
    void changesLandTypeInTargetPermanentLandwalkAbility() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Bog Wraith");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        Permanent bogWraith = findPermanent(player2, "Bog Wraith");
        assertThat(gqs.hasKeyword(gd, bogWraith, Keyword.SWAMPWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bogWraith, Keyword.PLAINSWALK)).isTrue();
    }

    @Test
    @DisplayName("Only basic land types may be chosen — a color word is rejected")
    void onlyOffersLandTypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "BLACK"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("A land-type change to a target spell carries onto the permanent it becomes (CR 400.7a)")
    void changesLandTypeOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new MagicalHack(), new BogWraith()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Bog Wraith creature spell goes on the stack (index 1; Magical Hack stays at index 0).
        harness.castCreature(player1, 1);
        UUID bogWraithSpellId = gd.stack.getFirst().getCard().getId();

        harness.castInstant(player1, 0, bogWraithSpellId);
        harness.passBothPriorities(); // resolve Magical Hack — begins the land-type choice

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "PLAINS");

        harness.passBothPriorities(); // resolve the Bog Wraith spell

        Permanent bogWraith = findPermanent(player1, "Bog Wraith");
        assertThat(gqs.hasKeyword(gd, bogWraith, Keyword.SWAMPWALK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bogWraith, Keyword.PLAINSWALK)).isTrue();
    }

    @Test
    @DisplayName("Cannot replace a basic land type with itself")
    void cannotReplaceLandTypeWithItself() {
        harness.addToBattlefield(player2, new BogWraith());
        harness.setHand(player1, List.of(new MagicalHack()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Bog Wraith");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");

        assertThatThrownBy(() -> harness.handleListChoice(player1, "SWAMP"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
