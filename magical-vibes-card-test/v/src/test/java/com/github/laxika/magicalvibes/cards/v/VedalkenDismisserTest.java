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

    @Test
    @DisplayName("Can put a creature you control on top without disturbing the rest of the library")
    void canTargetOwnCreature() {
        BorosRecruit targetCard = new BorosRecruit();
        BorosRecruit libraryCard = new BorosRecruit();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new VedalkenDismisser(), "{5}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Boros Recruit");
        harness.assertOnBattlefield(player1, "Vedalken Dismisser");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(targetCard, libraryCard);
    }

    @Test
    @DisplayName("Must target itself when it enters an otherwise empty battlefield")
    void targetsItselfWhenOnlyCreature() {
        VedalkenDismisser dismisser = new VedalkenDismisser();
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, dismisser, "{5}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Vedalken Dismisser"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Vedalken Dismisser");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dismisser);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The triggered ability resolves even if Dismisser leaves the battlefield")
    void abilityResolvesAfterSourceLeaves() {
        BorosRecruit targetCard = new BorosRecruit();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new VedalkenDismisser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        Permanent source = findPermanent(player1, "Vedalken Dismisser");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Vedalken Dismisser");
        harness.assertNotOnBattlefield(player2, "Boros Recruit");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetCard);
    }
}
