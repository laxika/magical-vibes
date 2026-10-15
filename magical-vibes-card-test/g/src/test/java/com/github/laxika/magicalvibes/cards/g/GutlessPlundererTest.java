package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GutlessPlunderer.class, Forest.class, SerraAngel.class})
class GutlessPlundererTest extends BaseCardTest {

    @Test
    @DisplayName("Raid offers the top three cards and keeps the chosen card on top")
    void raidKeepsChosenCardOnTopAndGravesTheRest() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card chosen = new Forest();
        Card restOne = new GutlessPlunderer();
        Card restTwo = new GutlessPlunderer();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
    }

    @Test
    @DisplayName("Declining the raid choice puts all three cards into the graveyard")
    void decliningGravesAllLookedAtCards() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card first = new Forest();
        Card second = new GutlessPlunderer();
        Card third = new GutlessPlunderer();
        harness.setLibrary(player1, List.of(first, second, third));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second, third);
    }

    @Test
    @DisplayName("Raid does not trigger when no creature was attacked with this turn")
    void noRaidDoesNothing() {
        List<Card> library = List.of(new Forest(), new GutlessPlunderer(), new GutlessPlunderer());
        harness.setLibrary(player1, library);

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Raid can keep a non-top card without disturbing the rest of the library")
    void keepsThirdCardAboveUntouchedLibrary() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card first = new Forest();
        Card second = new Forest();
        Card chosen = new GutlessPlunderer();
        Card fourth = new Forest();
        Card fifth = new GutlessPlunderer();
        harness.setLibrary(player1, List.of(first, second, chosen, fourth, fifth));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen, fourth, fifth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining raid leaves cards below the top three untouched")
    void decliningDoesNotMillBeyondThreeCards() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card first = new Forest();
        Card second = new GutlessPlunderer();
        Card third = new Forest();
        Card fourth = new GutlessPlunderer();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second, third);
    }

    @Test
    @DisplayName("Raid looks at all available cards when the library has fewer than three")
    void shortLibraryStillAllowsChoosingACard() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card first = new Forest();
        Card chosen = new GutlessPlunderer();
        harness.setLibrary(player1, List.of(first, chosen));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Raid allows putting the sole library card into the graveyard")
    void singleCardLibraryCanBeDeclined() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    @DisplayName("Raid with an empty library completes without a choice")
    void emptyLibraryDoesNotRequireAChoice() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.setLibrary(player1, List.of());

        castPlunderer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Gutless Plunderer");
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy raid")
    void opponentsAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        List<Card> library = List.of(new Forest(), new GutlessPlunderer(), new Forest());
        harness.setLibrary(player1, library);

        castPlunderer();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Raid triggers when Gutless Plunderer enters without being cast")
    void noncastEntryTriggersRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Card onlyCard = new Forest();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.enterBattlefieldAndReturn(player1, new GutlessPlunderer());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker with more toughness than the damage dealt")
    void deathtouchKillsLargerBlocker() {
        Permanent attacker = addCreatureReady(player1, new GutlessPlunderer());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Gutless Plunderer");
    }

    private void castPlunderer() {
        harness.castFromHand(player1, new GutlessPlunderer(), "{2}{B}");
    }
}
