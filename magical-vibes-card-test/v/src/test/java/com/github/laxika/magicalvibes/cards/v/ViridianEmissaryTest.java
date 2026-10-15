package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViridianEmissary.class, Forest.class, GrizzlyBears.class, Island.class, Plains.class, WrathOfGod.class})
class ViridianEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Viridian Emissary dies blocking, accept may, choose basic land — land enters tapped")
    void diesInCombatAcceptSearchChooseLand() {
        ViridianEmissary emissary = new ViridianEmissary();
        Permanent emissaryPerm = new Permanent(emissary);
        emissaryPerm.setSummoningSick(false);
        emissaryPerm.setBlocking(true);
        emissaryPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(emissaryPerm);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        setupLibrary(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage — emissary dies, MayEffect on stack

        GameData gd = harness.getGameData();

        // Emissary (2/1) should die after blocking a 2/2
        harness.assertInGraveyard(player1, "Viridian Emissary");

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        // Chosen land should be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("Viridian Emissary dies blocking, decline may — no search")
    void diesInCombatDeclineSearch() {
        ViridianEmissary emissary = new ViridianEmissary();
        Permanent emissaryPerm = new Permanent(emissary);
        emissaryPerm.setSummoningSick(false);
        emissaryPerm.setBlocking(true);
        emissaryPerm.addBlockingTarget(0);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(emissaryPerm);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent attacker = new Permanent(bears);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        int battlefieldBefore = harness.getGameData().playerBattlefields.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // combat damage — emissary dies, MayEffect on stack

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Viridian Emissary");

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No new permanents on battlefield (emissary died, nothing added)
        assertThat(gd.playerBattlefields.get(player1.getId()).size()).isLessThanOrEqualTo(battlefieldBefore);
    }

    @Test
    @DisplayName("Viridian Emissary dies from Wrath of God, accept may, choose basic land")
    void diesFromWrathAcceptSearch() {
        harness.addToBattlefield(player1, new ViridianEmissary());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupLibrary(player1);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // resolve Wrath — emissary dies, MayEffect on stack

        GameData gd = harness.getGameData();

        harness.assertInGraveyard(player1, "Viridian Emissary");

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    @Test
    @DisplayName("Viridian Emissary dies, accept may, fail to find — no land enters")
    void diesAcceptMayFailToFind() {
        harness.addToBattlefield(player1, new ViridianEmissary());

        // Library with only non-basic cards
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // resolve Wrath — emissary dies, MayEffect on stack

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // No library search prompt since no basic lands exist
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    @Test
    @DisplayName("May fail to find even when a basic land is available")
    void mayFailToFindWithBasicLandAvailable() {
        harness.addToBattlefield(player1, new ViridianEmissary());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent's dying Emissary searches the opponent's library")
    void opponentSearchesTheirOwnLibrary() {
        harness.addToBattlefield(player2, new ViridianEmissary());
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(plains));
        harness.setLibrary(player2, List.of(forest, new GrizzlyBears()));
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(forest);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isSameAs(forest);
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        harness.setLibrary(player, List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
