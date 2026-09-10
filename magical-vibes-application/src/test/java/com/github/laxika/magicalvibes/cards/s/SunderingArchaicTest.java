package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SunderingArchaicTest extends BaseCardTest {

    @Test
    @DisplayName("Converge exiles an opponent's nonland permanent within the color limit")
    void convergeExilesWithinColorLimit() {
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new SunderingArchaic()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID elfCardId = findPermanent(player2, "Llanowar Elves").getCard().getId();
        UUID elfId = harness.getPermanentId(player2, "Llanowar Elves");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(elfId).doesNotContain(giantId);
        harness.handlePermanentChosen(player1, elfId);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(elfCardId)).isNotNull();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Puts a target card from any graveyard on the bottom of its owner's library")
    void tucksCardFromAnyGraveyard() {
        harness.addToBattlefield(player1, new SunderingArchaic());
        Permanent archaic = findPermanent(player1, "Sundering Archaic");
        archaic.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player2, new ArrayList<>(List.of(new HillGiant())));

        int archaicIndex = gd.playerBattlefields.get(player1.getId()).indexOf(archaic);
        harness.activateAbilityWithGraveyardTargets(player1, archaicIndex, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(tucked.getId());
    }
}
