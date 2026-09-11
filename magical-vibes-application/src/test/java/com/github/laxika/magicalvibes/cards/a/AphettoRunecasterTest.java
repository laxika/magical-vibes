package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AphettoRunecaster.class, AinokTracker.class, Forest.class})
class AphettoRunecasterTest extends BaseCardTest {

    @Test
    void mayDrawWhenAnyPermanentTurnsFaceUp() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new AphettoRunecaster());
        Permanent faceDownPermanent = harness.addToBattlefieldAndReturn(player2, new AinokTracker());
        faceDownPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDownPermanent));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void mayDeclineTheDraw() {
        Card drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new AphettoRunecaster());
        Permanent faceDownPermanent = harness.addToBattlefieldAndReturn(player2, new AinokTracker());
        faceDownPermanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(faceDownPermanent));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).contains(drawn);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }
}
