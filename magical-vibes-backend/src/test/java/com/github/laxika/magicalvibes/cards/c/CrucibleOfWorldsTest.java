package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PlayLandsFromGraveyardEffect;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrucibleOfWorldsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Crucible of Worlds has correct card properties")
    void hasCorrectProperties() {
        CrucibleOfWorlds card = new CrucibleOfWorlds();

        assertThat(card.getName()).isEqualTo("Crucible of Worlds");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{3}");
        assertThat(card.getColor()).isNull();
        assertThat(card.getCardText()).isEqualTo("You may play lands from your graveyard.");
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PlayLandsFromGraveyardEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Crucible of Worlds can be cast as an artifact")
    void canBeCast() {
        harness.setHand(player1, List.of(new CrucibleOfWorlds()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Crucible of Worlds"));
    }

    // ===== Playing lands from graveyard =====

    @Test
    @DisplayName("Can play a land from graveyard with Crucible on battlefield")
    void canPlayLandFromGraveyard() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Playing land from graveyard counts as the land play for the turn")
    void usesNormalLandDrop() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest(), new Plains()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play first land from graveyard
        harness.playGraveyardLand(player1, 0);

        // Second graveyard land should not be playable
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard if already played a land from hand")
    void cannotPlayIfAlreadyPlayedFromHand() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        Plains handPlains = new Plains();
        harness.setHand(player1, List.of(handPlains));
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play a land from hand first
        gs.playCard(gd, player1, 0, 0, null, null);

        // Graveyard land should not be playable
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from hand if already played from graveyard")
    void cannotPlayFromHandIfAlreadyPlayedFromGraveyard() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Plains()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Play a land from graveyard first
        harness.playGraveyardLand(player1, 0);

        // Hand land should not be playable
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Crucible must be on battlefield =====

    @Test
    @DisplayName("Cannot play land from graveyard without Crucible on battlefield")
    void cannotPlayWithoutCrucible() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Removing Crucible disables playing lands from graveyard")
    void removingCrucibleDisablesAbility() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Remove Crucible from battlefield
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Crucible of Worlds"));

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    // ===== Only lands are playable =====

    @Test
    @DisplayName("Creatures in graveyard are not playable via Crucible")
    void creaturesInGraveyardNotPlayable() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    // ===== Timing restrictions =====

    @Test
    @DisplayName("Cannot play land from graveyard during opponent's turn")
    void cannotPlayDuringOpponentTurn() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Cannot play land from graveyard during combat")
    void cannotPlayDuringCombat() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    // ===== Only affects controller =====

    @Test
    @DisplayName("Crucible only allows its controller to play lands from graveyard")
    void onlyAffectsController() {
        // Player1 controls Crucible, but player2 tries to play from their graveyard
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    // ===== Can still play lands from hand normally =====

    @Test
    @DisplayName("Can still play lands from hand normally with Crucible on battlefield")
    void canStillPlayFromHand() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setHand(player1, List.of(new Plains()));
        harness.setGraveyard(player1, List.of(new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
    }

    // ===== Postcombat main phase =====

    @Test
    @DisplayName("Can play land from graveyard during postcombat main phase")
    void worksInPostcombatMain() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
    }

    // ===== Plays the correct card from graveyard =====

    @Test
    @DisplayName("Plays the correct land when multiple cards are in graveyard")
    void playsCorrectLandFromGraveyard() {
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setGraveyard(player1, List.of(bears, forest, plains));
        harness.setHand(player1, List.of());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Index 1 is the Forest (index 0 is GrizzlyBears which is not a land)
        harness.playGraveyardLand(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
    }
}
