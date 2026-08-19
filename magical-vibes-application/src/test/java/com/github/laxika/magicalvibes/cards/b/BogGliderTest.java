package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CateranPersuader;
import com.github.laxika.magicalvibes.cards.c.CateranSlaver;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BogGliderTest extends BaseCardTest {

    @Test
    void sacrificesALandToPutEligibleMercenaryOntoBattlefield() {
        Permanent glider = addCreatureReady(player1, new BogGlider());
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new CateranPersuader(), new CateranSlaver(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        assertThat(glider.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Cateran Persuader");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Cateran Persuader");
        harness.assertNotOnBattlefield(player1, "Cateran Slaver");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
