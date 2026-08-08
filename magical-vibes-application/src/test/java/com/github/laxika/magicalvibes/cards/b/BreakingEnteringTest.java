package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BreakingEnteringTest extends BaseCardTest {

    private static final int BREAKING = 0;
    private static final int ENTERING = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Breaking mills eight cards from the target player's library")
    void breakingMillsEightCards() {
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, BREAKING, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(8);
    }

    @Test
    @DisplayName("Entering returns a creature from any graveyard with haste")
    void enteringReturnsCreatureWithHaste() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, ENTERING, target.getId());
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getCard()).isSameAs(target);
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fuse resolves Breaking before Entering")
    void fuseMillsThenReturnsCreatureWithHaste() {
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new BreakingEntering()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, FUSE, target.getId(), null,
                List.of(player2.getId(), target.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        Permanent returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getCard()).isSameAs(target);
        assertThat(returned.getGrantedKeywords()).contains(Keyword.HASTE);
    }
}
