package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RangingRaptorsTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Ranging Raptors has correct ON_DEALT_DAMAGE enrage effect")
    void hasCorrectEffect() {
        RangingRaptors card = new RangingRaptors();

        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DEALT_DAMAGE).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SearchLibraryForCardTypesToBattlefieldEffect.class);
        SearchLibraryForCardTypesToBattlefieldEffect search =
                (SearchLibraryForCardTypesToBattlefieldEffect) may.wrapped();
        assertThat(CardPredicateUtils.describeFilter(search.filter())).isEqualTo("basic land card");
        assertThat(search.entersTapped()).isTrue();
    }

    // ===== Spell damage trigger: accept =====

    @Test
    @DisplayName("Shock deals damage to Ranging Raptors, accept may, choose basic land — enters tapped, Raptors survives")
    void spellDamageAcceptSearch() {
        harness.addToBattlefield(player1, new RangingRaptors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        setupLibrary(player1);

        UUID raptorsId = harness.getPermanentId(player1, "Ranging Raptors");
        harness.castInstant(player2, 0, raptorsId);
        harness.passBothPriorities(); // resolve Shock — 2 damage to Ranging Raptors (survives at 2/3)

        GameData gd = harness.getGameData();

        // ON_DEALT_DAMAGE trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve MayEffect → may prompt

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.getGameService().handleLibraryCardChosen(gd, player1, 0);

        // Chosen land should be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());

        // Ranging Raptors should survive (2 damage to a 2/3)
        harness.assertOnBattlefield(player1, "Ranging Raptors");
    }

    // ===== Spell damage trigger: decline =====

    @Test
    @DisplayName("Shock deals damage to Ranging Raptors, decline may — no search")
    void spellDamageDeclineSearch() {
        harness.addToBattlefield(player1, new RangingRaptors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID raptorsId = harness.getPermanentId(player1, "Ranging Raptors");
        harness.castInstant(player2, 0, raptorsId);
        harness.passBothPriorities(); // resolve Shock

        GameData gd = harness.getGameData();

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        // No new permanents on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        harness.assertOnBattlefield(player1, "Ranging Raptors");
    }

    // ===== Combat damage trigger: non-lethal =====

    @Test
    @DisplayName("Ranging Raptors takes non-lethal combat damage, accept may, choose basic land")
    void nonLethalCombatDamageAcceptSearch() {
        harness.addToBattlefield(player2, new RangingRaptors());
        harness.addToBattlefield(player1, new GrizzlyBears()); // 2/2

        setupLibrary(player2);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent raptors = gd.playerBattlefields.get(player2.getId()).getFirst();
        raptors.setSummoningSick(false);
        raptors.setBlocking(true);
        raptors.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to combat damage
        harness.passBothPriorities(); // deal combat damage, triggers go on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();

        // Grizzly Bears (2/2) should die from 2 damage by Ranging Raptors (2/3)
        harness.assertInGraveyard(player1, "Grizzly Bears");

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        harness.getGameService().handleLibraryCardChosen(gd, player2, 0);

        // Land should be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());

        // Ranging Raptors should survive (2/3 took 2 damage)
        harness.assertOnBattlefield(player2, "Ranging Raptors");
    }

    // ===== Combat damage trigger: lethal =====

    @Test
    @DisplayName("Ranging Raptors takes lethal combat damage, enrage still triggers")
    void lethalCombatDamageStillTriggers() {
        harness.addToBattlefield(player2, new RangingRaptors());
        harness.addToBattlefield(player1, new HillGiant()); // 3/3 — lethal for 2/3 Raptors

        setupLibrary(player2);

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent raptors = gd.playerBattlefields.get(player2.getId()).getFirst();
        raptors.setSummoningSick(false);
        raptors.setBlocking(true);
        raptors.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities(); // advance to combat damage
        harness.passBothPriorities(); // deal combat damage, triggers go on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        harness.getGameService().handleLibraryCardChosen(gd, player2, 0);

        // Ranging Raptors should be in the graveyard (2/3 took 3 lethal from Hill Giant)
        harness.assertInGraveyard(player2, "Ranging Raptors");

        // Land should still be on the battlefield tapped
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().hasType(CardType.LAND) && p.isTapped());
    }

    // ===== No basic lands in library =====

    @Test
    @DisplayName("Ranging Raptors enrage triggers, accept may, no basic lands — no search prompt")
    void noBasicLandsNoPrompt() {
        harness.addToBattlefield(player1, new RangingRaptors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        // Library with only non-basic cards
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        UUID raptorsId = harness.getPermanentId(player1, "Ranging Raptors");
        harness.castInstant(player2, 0, raptorsId);
        harness.passBothPriorities(); // resolve Shock

        GameData gd = harness.getGameData();

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true);

        // No library search prompt since no basic lands exist
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().hasType(CardType.LAND));
    }

    private void setupLibrary(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> deck = harness.getGameData().playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(new Plains(), new Forest(), new Island(), new GrizzlyBears()));
    }
}
