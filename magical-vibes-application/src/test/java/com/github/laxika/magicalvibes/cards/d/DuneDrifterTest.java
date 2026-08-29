package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DuneDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target artifact or creature with mana value X or less")
    void etbReturnsEligibleArtifactOrCreature() {
        Card artifact = new Spellbook();
        Card creature = new GrizzlyBears();
        Card tooExpensive = new AvatarOfMight();
        harness.setGraveyard(player1, List.of(artifact, creature, tooExpensive));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("Crew 2 turns Dune Drifter into a creature until end of turn")
    void crewsDuneDrifter() {
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new DuneDrifter());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, drifter)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not target a card with mana value greater than X")
    void etbDoesNotTargetTooExpensiveCard() {
        Card tooExpensive = new AvatarOfMight();
        harness.setGraveyard(player1, List.of(tooExpensive));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    private void addDuneDrifterMana(int xValue) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
