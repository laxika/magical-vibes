package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LabRats;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RishadanPawnshopTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles a nontoken permanent you control into its owner's library")
    void shufflesControlledNontokenPermanentIntoOwnersLibrary() {
        Permanent pawnshop = harness.addToBattlefieldAndReturn(player1, new RishadanPawnshop());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player1.getId()))
                .hasSize(deckSizeBefore + 1)
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(pawnshop.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a token or a permanent controlled by an opponent")
    void rejectsTokenAndOpponentPermanent() {
        harness.addToBattlefield(player1, new RishadanPawnshop());
        harness.setHand(player1, List.of(new LabRats()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        Permanent rat = findPermanent(player1, "Rat");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, rat.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
