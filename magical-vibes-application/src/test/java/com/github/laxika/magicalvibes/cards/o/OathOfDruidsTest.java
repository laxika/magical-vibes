package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CrashingBoars;
import com.github.laxika.magicalvibes.cards.e.ElvenPalisade;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OathOfDruids.class, ElvenPalisade.class, CrashingBoars.class})
class OathOfDruidsTest extends BaseCardTest {

    @Test
    @DisplayName("The active player reveals their library and puts the first creature onto the battlefield")
    void activePlayerRevealsTheirLibrary() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player2, new CrashingBoars());
        harness.setLibrary(player1, List.of(new ElvenPalisade(), new CrashingBoars()));

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Oath of Druids", "Crashing Boars");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elven Palisade");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Crashing Boars");
    }

    @Test
    @DisplayName("The active player reveals their own library when another player controls the Oath")
    void activePlayerOwnsRevealWhenOpponentControlsOath() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player1, new CrashingBoars());
        harness.setLibrary(player1, List.of(new ElvenPalisade()));
        harness.setLibrary(player2, List.of(new ElvenPalisade(), new CrashingBoars()));

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Crashing Boars");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Elven Palisade");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Oath of Druids", "Crashing Boars");
    }

    @Test
    @DisplayName("The active player may decline the reveal")
    void mayDeclineReveal() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player2, new CrashingBoars());
        harness.setLibrary(player1, List.of(new ElvenPalisade(), new CrashingBoars()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elven Palisade", "Crashing Boars");
        harness.assertOnBattlefield(player1, "Oath of Druids");
    }

    @Test
    @DisplayName("Accepting with no creature in the library puts every revealed card into the graveyard")
    void acceptingWithNoCreatureInLibraryPutsEveryRevealedCardIntoGraveyard() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player2, new CrashingBoars());
        harness.setLibrary(player1, List.of(new ElvenPalisade(), new ElvenPalisade()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elven Palisade", "Elven Palisade");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Oath of Druids");
    }

    @Test
    @DisplayName("The ability does not offer a reveal when no opponent controls more creatures")
    void doesNotOfferRevealWithoutOpponentWithMoreCreatures() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player1, new CrashingBoars());
        harness.addToBattlefield(player2, new CrashingBoars());
        harness.setLibrary(player1, List.of(new ElvenPalisade(), new CrashingBoars()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elven Palisade", "Crashing Boars");
    }

    @Test
    @DisplayName("The ability does not resolve when the target no longer controls more creatures")
    void doesNotResolveWhenTargetNoLongerControlsMoreCreatures() {
        harness.addToBattlefield(player1, new OathOfDruids());
        harness.addToBattlefield(player2, new CrashingBoars());
        harness.setLibrary(player1, List.of(new ElvenPalisade(), new CrashingBoars()));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.addToBattlefield(player1, new CrashingBoars());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Elven Palisade", "Crashing Boars");
    }
}
