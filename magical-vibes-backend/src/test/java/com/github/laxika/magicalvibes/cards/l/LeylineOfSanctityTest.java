package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantControllerHexproofEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("scryfall")
class LeylineOfSanctityTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — leyline tests need to set hand first
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Leyline of Sanctity has GrantControllerHexproofEffect as static effect")
    void hasGrantControllerHexproofStaticEffect() {
        LeylineOfSanctity card = new LeylineOfSanctity();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantControllerHexproofEffect.class);
    }

    @Test
    @DisplayName("Leyline of Sanctity has ON_OPENING_HAND_REVEAL MayEffect wrapping LeylineStartOnBattlefieldEffect")
    void hasOpeningHandLeylineEffect() {
        LeylineOfSanctity card = new LeylineOfSanctity();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(LeylineStartOnBattlefieldEffect.class);
    }

    // ===== Leyline opening hand mechanic (CR 103.6) =====

    @Test
    @DisplayName("Leyline in opening hand prompts may ability at game start")
    void leylineInOpeningHandPromptsChoice() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Sanctity"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leyline of Sanctity"));
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leyline of Sanctity"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leyline of Sanctity"));
    }

    // ===== Player hexproof — opponents cannot target player =====

    @Test
    @DisplayName("Opponent cannot target player with a spell when Leyline of Sanctity is on battlefield")
    void opponentCannotTargetPlayerWithSpell() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Player can still target themselves with a spell when they have Leyline of Sanctity")
    void canTargetSelfWithOwnLeyline() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Player can still target themselves when opponent has Leyline of Sanctity")
    void canTargetSelfWhenOpponentHasLeyline() {
        harness.skipMulligan();
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    // ===== Hexproof does not protect creatures =====

    @Test
    @DisplayName("Leyline of Sanctity grants hexproof to the player, not to creatures")
    void hexproofProtectsPlayerNotCreatures() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.addToBattlefield(player1, new GrizzlyBears());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    // ===== Hexproof only while on battlefield =====

    @Test
    @DisplayName("Player can be targeted after Leyline of Sanctity is removed from battlefield")
    void canTargetPlayerAfterLeylineRemoved() {
        harness.skipMulligan();
        LeylineOfSanctity leyline = new LeylineOfSanctity();
        harness.addToBattlefield(player1, leyline);

        // Remove Leyline from battlefield
        GameData gd = harness.getGameData();
        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leyline of Sanctity"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(leyline);

        // Now the player can be targeted
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    // ===== Leyline can be cast normally from hand =====

    @Test
    @DisplayName("Leyline of Sanctity can be cast normally for {2}{W}{W}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Sanctity"));
    }

    // ===== Leyline placement from opening hand grants hexproof =====

    @Test
    @DisplayName("Leyline placed from opening hand immediately grants hexproof")
    void leylineFromOpeningHandGrantsHexproof() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Leyline placement is logged")
    void leylinePlacementIsLogged() {
        harness.setHand(player1, List.of(new LeylineOfSanctity()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("begins the game with Leyline of Sanctity"));
    }
}
