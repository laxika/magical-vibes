package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NecromanticSelection.class, GrizzlyBears.class, HillGiant.class})
class NecromanticSelectionTest extends BaseCardTest {

    @Test
    void returnsOneDestroyedCreatureAsBlackZombieUnderYourControlAndExilesSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Card preexistingCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(preexistingCreature));
        NecromanticSelection selection = new NecromanticSelection();
        harness.setHand(player1, List.of(selection));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cardPool()).extracting(Card::getId)
                .containsExactly(bears.getCard().getId(), giant.getCard().getId());

        harness.handleGraveyardCardChosen(player1, choice.cardPool().indexOf(giant.getCard()));

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(giant.getCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectiveColors(gd, returned)).contains(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, returned))
                .contains(CardSubtype.GIANT, CardSubtype.ZOMBIE);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }

    @Test
    void doesNotReturnCreatureAlreadyInGraveyard() {
        Card preexistingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(preexistingCreature));
        NecromanticSelection selection = new NecromanticSelection();
        harness.setHand(player1, List.of(selection));
        harness.addMana(player1, ManaColor.BLACK, 8);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(selection.getId())).isNotNull();
    }
}
