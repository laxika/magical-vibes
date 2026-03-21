package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeylineOfAnticipationTest {

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
    @DisplayName("Leyline of Anticipation has GrantFlashToCardTypeEffect with null (all types)")
    void hasGrantFlashStaticEffect() {
        LeylineOfAnticipation card = new LeylineOfAnticipation();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantFlashToCardTypeEffect.class);
        GrantFlashToCardTypeEffect effect = (GrantFlashToCardTypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.filter()).isNull();
    }

    @Test
    @DisplayName("Leyline of Anticipation has ON_OPENING_HAND_REVEAL MayEffect wrapping LeylineStartOnBattlefieldEffect")
    void hasOpeningHandLeylineEffect() {
        LeylineOfAnticipation card = new LeylineOfAnticipation();

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
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Accepting leyline places it on the battlefield from hand")
    void acceptingLeylinePlacesOnBattlefield() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Anticipation"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leyline of Anticipation"));
    }

    @Test
    @DisplayName("Declining leyline keeps it in hand")
    void decliningLeylineKeepsInHand() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leyline of Anticipation"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leyline of Anticipation"));
    }

    @Test
    @DisplayName("Declined leyline is not re-prompted during the first upkeep")
    void declinedLeylineNotRePromptedDuringUpkeep() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        // Decline the leyline — card stays in hand
        harness.handleMayAbilityChosen(player1, false);

        // Game should be running now without re-prompting
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leyline of Anticipation"));
    }

    @Test
    @DisplayName("Leyline placement is logged")
    void leylinePlacementIsLogged() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("begins the game with Leyline of Anticipation"));
    }

    @Test
    @DisplayName("Multiple leylines in opening hand each prompt separately")
    void multipleLeylinesPromptSeparately() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation(), new LeylineOfAnticipation()));
        harness.skipMulligan();

        // Accept first leyline
        harness.handleMayAbilityChosen(player1, true);
        // Accept second leyline
        harness.handleMayAbilityChosen(player1, true);

        long count = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leyline of Anticipation"))
                .count();
        assertThat(count).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both players can start with leylines on the battlefield")
    void bothPlayersCanHaveLeylines() {
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.setHand(player2, List.of(new LeylineOfAnticipation()));
        harness.skipMulligan();

        // Accept both — per CR 103.6, starting player acts first
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Anticipation"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Anticipation"));
    }

    // ===== Grant flash to all spells =====

    @Test
    @DisplayName("Can cast creature at instant speed with Leyline of Anticipation on battlefield")
    void canCastCreatureAtInstantSpeed() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast creature during opponent's turn with Leyline of Anticipation on battlefield")
    void canCastCreatureDuringOpponentsTurn() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        // Player2 passes priority, giving player1 priority
        harness.getGameService().passPriority(harness.getGameData(), player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Can cast sorcery at instant speed with Leyline of Anticipation on battlefield")
    void canCastSorceryAtInstantSpeed() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lava Axe");
    }

    // ===== Only affects controller =====

    @Test
    @DisplayName("Leyline of Anticipation only grants flash to its controller's spells")
    void onlyAffectsController() {
        harness.skipMulligan();
        harness.addToBattlefield(player2, new LeylineOfAnticipation());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Effect goes away when Leyline leaves =====

    @Test
    @DisplayName("Spells lose flash timing when Leyline of Anticipation leaves the battlefield")
    void spellsLoseFlashWhenLeylineLeaves() {
        harness.skipMulligan();
        harness.addToBattlefield(player1, new LeylineOfAnticipation());

        // Remove Leyline from battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Leyline can be cast normally from hand =====

    @Test
    @DisplayName("Leyline of Anticipation can be cast normally for {2}{U}{U}")
    void canBeCastNormally() {
        harness.skipMulligan();
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Anticipation"));
    }
}
