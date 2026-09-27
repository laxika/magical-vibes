package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
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

@CardUsed({VedalkenDismisser.class, BorosRecruit.class})
class VedalkenDismisserTest extends BaseCardTest {

    @Test
    @DisplayName("When Vedalken Dismisser enters, it puts target creature on top of its owner's library")
    void putsTargetCreatureOnTopOfOwnersLibrary() {
        harness.addToBattlefield(player2, new BorosRecruit());
        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");
        harness.setHand(player1, List.of(new VedalkenDismisser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(targetId));
        resolveAllTriggers();

        GameData gameData = harness.getGameData();
        harness.assertOnBattlefield(player1, "Vedalken Dismisser");
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        assertThat(gameData.playerDecks.get(player2.getId()))
                .first()
                .extracting(Card::getName)
                .isEqualTo("Boros Recruit");
    }

    @Test
    @DisplayName("The enters-the-battlefield ability fizzles if the target creature leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BorosRecruit());
        UUID targetId = harness.getPermanentId(player2, "Boros Recruit");
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new VedalkenDismisser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0, List.of(targetId));
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Boros Recruit");
        harness.assertOnBattlefield(player1, "Vedalken Dismisser");
    }
}
