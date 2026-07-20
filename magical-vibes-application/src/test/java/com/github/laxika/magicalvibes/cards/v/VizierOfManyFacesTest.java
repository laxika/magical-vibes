package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VizierOfManyFacesTest extends BaseCardTest {

    private Permanent enteredVizier() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Vizier of Many Faces"))
                .findFirst().orElse(null);
    }

    // ===== Hard-cast: a plain Clone, the embalm exception must NOT apply =====

    @Test
    @DisplayName("Hard-cast copies a creature without the embalm transformation")
    void hardCastCopiesWithoutEmbalmTransformation() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent vizier = enteredVizier();
        assertThat(vizier).isNotNull();
        // It's a straight copy of Grizzly Bears: keeps the copied creature's color, cost, and types.
        assertThat(vizier.getCard().getPower()).isEqualTo(2);
        assertThat(vizier.getCard().getToughness()).isEqualTo(2);
        assertThat(vizier.getCard().getColor()).isNotEqualTo(CardColor.WHITE);
        assertThat(vizier.getCard().getSubtypes()).contains(CardSubtype.BEAR).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(vizier.getCard().getManaCost()).isNotEmpty();
    }

    // ===== Embalm: the token re-clones and the copy becomes a white Zombie with no mana cost =====

    @Test
    @DisplayName("Embalmed token enters as a copy that is white, a Zombie, and has no mana cost")
    void embalmTokenCopyGetsEmbalmTransformation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5); // pays {3}{U}{U}

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve Embalm → token's copy-on-enter may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent token = enteredVizier();
        assertThat(token).isNotNull();
        assertThat(token.getCard().isToken()).isTrue();
        // Copied Grizzly Bears' body...
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        // ...but transformed by the embalm exception on the final copy.
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.BEAR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalmed token declining to copy enters as a 0/0 and dies")
    void embalmTokenDiesWhenDecliningToCopy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new VizierOfManyFaces()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false); // decline → enters as a white Zombie 0/0

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Vizier of Many Faces"));
    }
}
