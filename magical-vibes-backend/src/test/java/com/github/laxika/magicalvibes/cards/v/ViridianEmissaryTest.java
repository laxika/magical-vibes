package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardTypesToBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViridianEmissaryTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Viridian Emissary has correct death trigger effect")
    void hasCorrectProperties() {
        ViridianEmissary card = new ViridianEmissary();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);
        SearchLibraryForCardTypesToBattlefieldEffect search =
                (SearchLibraryForCardTypesToBattlefieldEffect) may.wrapped();
        assertThat(search.cardTypes()).containsExactly(CardType.LAND);
        assertThat(search.requiresBasicSupertype()).isTrue();
        assertThat(search.entersTapped()).isTrue();
    }

    // ===== Death trigger: combat (blocker dies), accept =====

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
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Viridian Emissary"));

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline → library search

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Chosen land should be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getType() == CardType.LAND && p.isTapped());
    }

    // ===== Death trigger: combat (blocker dies), decline =====

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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Viridian Emissary"));

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No new permanents on battlefield (emissary died, nothing added)
        assertThat(gd.playerBattlefields.get(player1.getId()).size()).isLessThanOrEqualTo(battlefieldBefore);
    }

    // ===== Death trigger: Wrath of God, accept =====

    @Test
    @DisplayName("Viridian Emissary dies from Wrath of God, accept may, choose basic land")
    void diesFromWrathAcceptSearch() {
        harness.addToBattlefield(player1, new ViridianEmissary());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        setupLibrary(player1);

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — emissary dies, MayEffect on stack

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Viridian Emissary"));

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getType() == CardType.LAND && p.isTapped());
    }

    // ===== Death trigger: accept may, fail to find =====

    @Test
    @DisplayName("Viridian Emissary dies, accept may, fail to find — no land enters")
    void diesAcceptMayFailToFind() {
        harness.addToBattlefield(player1, new ViridianEmissary());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        // Library with only non-basic cards
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — emissary dies, MayEffect on stack

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // inner effect resolves inline

        // No library search prompt since no basic lands exist
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getType() == CardType.LAND);
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
