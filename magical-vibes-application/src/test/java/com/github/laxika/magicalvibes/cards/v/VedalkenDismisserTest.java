package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VedalkenDismisser.class, GrizzlyBears.class})
class VedalkenDismisserTest extends BaseCardTest {

    @Test
    @DisplayName("When Vedalken Dismisser enters, it puts target creature on top of its owner's library")
    void putsTargetCreatureOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new VedalkenDismisser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertOnBattlefield(player1, "Vedalken Dismisser");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .first()
                .extracting(Card::getName)
                .isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("The enters-the-battlefield ability fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new VedalkenDismisser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities();
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Vedalken Dismisser");
    }
}
