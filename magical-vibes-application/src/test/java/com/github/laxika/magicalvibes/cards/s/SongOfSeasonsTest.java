package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.Taiga;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongOfSeasons.class, Mountain.class, Forest.class, Plains.class, Taiga.class})
class SongOfSeasonsTest extends BaseCardTest {

    @Test
    void seeksEachLandKindAndLetsTheControllerChooseBattlefieldAndGraveyardCards() {
        Card mountain = new Mountain();
        Card forest = new Forest();
        Card plains = new Plains();
        Card taiga = new Taiga();
        harness.setLibrary(player1, List.of(mountain, forest, taiga, plains));
        harness.setHand(player1, List.of(new SongOfSeasons()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardFromHandIntoGraveyardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream().map(permanent -> permanent.getCard()))
                .containsExactly(mountain);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(taiga);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
