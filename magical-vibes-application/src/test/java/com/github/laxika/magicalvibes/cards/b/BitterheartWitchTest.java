package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BitterheartWitchTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Bitterheart Witch has correct death trigger effect")
    void hasCorrectProperties() {
        BitterheartWitch card = new BitterheartWitch();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(mayEffect.wrapped())
                .isInstanceOf(SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect.class);
        SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect effect =
                (SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect) mayEffect.wrapped();
        assertThat(effect.requiredSubtype()).isEqualTo(CardSubtype.CURSE);
        assertThat(mayEffect.canTargetPlayer()).isTrue();
    }

    // ===== Death trigger: dies in combat, search for Curse, attach to target player =====

    @Test
    @DisplayName("When Bitterheart Witch dies, controller is prompted to choose a target player")
    void deathTriggerPromptsForTargetPlayer() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Witch should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bitterheart Witch"));

        // Player1 should be prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger searches library for Curse card and puts it onto battlefield attached to target player")
    void deathTriggerSearchesForCurseAndAttaches() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card fakeCurse = createFakeCurseCard();
        setupLibraryWithCurse(player1, fakeCurse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player (player2)
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Bitterheart Witch");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        // Resolve the triggered ability — "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        // Choose the curse card (index 0)
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        gs.handleLibraryCardChosen(gd, player1, 0);

        // Curse should be on the battlefield under controller's control
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1);

        // Curse should be attached to target player
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(cursePerm.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Death trigger can target self (attach Curse to own player)")
    void deathTriggerCanTargetSelf() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card fakeCurse = createFakeCurseCard();
        setupLibraryWithCurse(player1, fakeCurse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose self as target
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities(); // resolve triggered ability → "you may search" prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        gs.handleLibraryCardChosen(gd, player1, 0);

        // Curse should be attached to player1
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(cursePerm.getAttachedTo()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger with no Curse in library — search finds nothing, library is shuffled")
    void deathTriggerNoCurseInLibrary() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Library with no curse cards
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new GrizzlyBears(), new GrizzlyBears()));

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability — "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        // No curse cards in library — no library search opened
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no Curse cards"));
    }

    @Test
    @DisplayName("Death trigger with fail to find — no Curse enters battlefield")
    void deathTriggerFailToFind() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card fakeCurse = createFakeCurseCard();
        setupLibraryWithCurse(player1, fakeCurse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability → "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Fail to find (index -1)
        gs.handleLibraryCardChosen(gd, player1, -1);

        // No new permanent on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    }

    @Test
    @DisplayName("Death trigger declined — player chooses not to search, no Curse enters battlefield")
    void deathTriggerDeclinedMay() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card fakeCurse = createFakeCurseCard();
        setupLibraryWithCurse(player1, fakeCurse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        // Choose target player
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve the triggered ability → "you may search" prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        // Decline the search
        harness.handleMayAbilityChosen(player1, false);

        // No library search opened, no new permanent on the battlefield
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.LIBRARY_SEARCH);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
    }

    @Test
    @DisplayName("Curse attached to player is not removed as orphaned aura")
    void curseAttachedToPlayerNotOrphaned() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card fakeCurse = createFakeCurseCard();
        setupLibraryWithCurse(player1, fakeCurse);

        setupCombatWhereWitchDies();

        harness.passBothPriorities(); // combat damage — witch dies

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve → "you may search" prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        gs.handleLibraryCardChosen(gd, player1, 0);

        // Verify curse is on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE));

        // Advance through several steps to trigger SBA / orphan aura checks
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Curse should still be on the battlefield (not removed as orphaned)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE));
    }

    // ===== Wrath of God - dies from mass removal =====

    @Test
    @DisplayName("Bitterheart Witch dies from Wrath of God — death trigger still fires")
    void diesFromWrathOfGod() {
        harness.addToBattlefield(player1, new BitterheartWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card fakeCurse = createFakeCurseCard();
        setupLibraryWithCurse(player1, fakeCurse);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath — all creatures die

        // Witch should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Bitterheart Witch"));

        // Player1 should be prompted to choose a target player
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // resolve triggered ability → "you may search" prompt

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.LIBRARY_SEARCH);

        gs.handleLibraryCardChosen(gd, player1, 0);

        // Curse should be on the battlefield attached to player2
        Permanent cursePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CURSE))
                .findFirst().orElseThrow();
        assertThat(cursePerm.getAttachedTo()).isEqualTo(player2.getId());
    }

    // ===== Helpers =====

    private void setupCombatWhereWitchDies() {
        Permanent witchPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Bitterheart Witch"))
                .findFirst().orElseThrow();
        witchPerm.setSummoningSick(false);
        witchPerm.setAttacking(true);

        // Witch is 1/2, needs to be blocked by something that kills it
        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    private Card createFakeCurseCard() {
        GrizzlyBears fakeCurse = new GrizzlyBears();
        fakeCurse.setSubtypes(List.of(CardSubtype.AURA, CardSubtype.CURSE));
        fakeCurse.setType(CardType.ENCHANTMENT);
        return fakeCurse;
    }

    private void setupLibraryWithCurse(com.github.laxika.magicalvibes.model.Player player, Card curseCard) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(List.of(curseCard, new GrizzlyBears()));
    }
}
