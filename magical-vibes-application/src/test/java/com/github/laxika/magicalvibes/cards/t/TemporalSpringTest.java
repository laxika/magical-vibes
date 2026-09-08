package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemporalSpring.class, Forest.class})
class TemporalSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Puts any target permanent on top of its owner's library")
    void putsTargetPermanentOnTopOfOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        UUID targetId = target.getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new TemporalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gameData.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
        assertThat(gameData.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .first()
                .isSameAs(target.getCard());
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .contains("Temporal Spring");
    }

    @Test
    @DisplayName("Fizzles if the target permanent is removed before resolution")
    void fizzlesIfTargetIsRemovedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new TemporalSpring()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }
}
