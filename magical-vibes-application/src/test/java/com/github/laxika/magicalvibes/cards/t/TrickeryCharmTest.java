package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrickeryCharm.class, GrizzlyBears.class})
class TrickeryCharmTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode gives target creature flying until end of turn")
    void givesFlyingUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCharm(player1, 0, creature.getId());

        assertThat(creature.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The second mode changes target creature to the chosen type until end of turn")
    void changesTargetCreatureTypeUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCharm(player1, 1, creature.getId());
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The third mode reorders the top four cards")
    void reordersTopFourCards() {
        List<Card> library = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears());
        harness.setLibrary(player1, library);

        castCharm(player1, 2, null);

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactlyElementsOf(library.subList(0, 4));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                library.get(3), library.get(1), library.get(0), library.get(2), library.get(4));
    }

    @Test
    @DisplayName("Creature modes reject a noncreature target")
    void rejectsNoncreatureTarget() {
        assertThatThrownBy(() -> castCharm(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot target players");
    }

    private void castCharm(Player caster, int mode, java.util.UUID targetId) {
        harness.setHand(caster, List.of(new TrickeryCharm()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castInstant(caster, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
