package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Ellie and Alan, Paleontologists")
@CardUsed({EllieAndAlanPaleontologists.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class EllieAndAlanPaleontologistsTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers up to the mana value of the exiled creature")
    void discoversUpToExiledCreatureManaValue() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest(), discovered));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Uses zero when the exiled creature has mana value zero")
    void usesZeroForZeroManaValueCreature() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());
        GrizzlyBears discovered = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Ornithopter()));
        harness.setLibrary(player1, List.of(discovered));

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discovered);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(discovered);
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        addCreatureReady(player1, new EllieAndAlanPaleontologists());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void canOnlyBeActivatedAtSorcerySpeed() {
        Permanent source = addCreatureReady(player1, new EllieAndAlanPaleontologists());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
