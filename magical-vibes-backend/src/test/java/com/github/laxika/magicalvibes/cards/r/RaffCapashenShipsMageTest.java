package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToCardTypeEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RaffCapashenShipsMageTest extends BaseCardTest {

    // ===== Static effect registration =====

    @Test
    @DisplayName("Raff Capashen has GrantFlashToCardTypeEffect with CardIsHistoricPredicate")
    void hasGrantFlashStaticEffect() {
        RaffCapashenShipsMage card = new RaffCapashenShipsMage();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(GrantFlashToCardTypeEffect.class);
        GrantFlashToCardTypeEffect effect = (GrantFlashToCardTypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.filter()).isInstanceOf(CardIsHistoricPredicate.class);
    }

    // ===== Grant flash to artifact spells (historic) =====

    @Test
    @DisplayName("Can cast artifact spell during opponent's turn with Raff on battlefield")
    void canCastArtifactDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Player2 passes priority, giving player1 priority
        harness.getGameService().passPriority(harness.getGameData(), player2);

        // Player1 can cast artifact with flash timing
        harness.castArtifact(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Leonin Scimitar");
    }

    // ===== Grant flash to legendary creature spells (historic) =====

    @Test
    @DisplayName("Can cast legendary creature during combat with Raff on battlefield")
    void canCastLegendaryCreatureDuringCombat() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new ReyaDawnbringer()));
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Reya Dawnbringer");
    }

    // ===== Does NOT grant flash to non-historic spells =====

    @Test
    @DisplayName("Cannot cast non-historic creature at instant speed with Raff on battlefield")
    void cannotCastNonHistoricCreatureAtInstantSpeed() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Effect goes away when Raff leaves =====

    @Test
    @DisplayName("Artifact spells lose flash timing when Raff leaves the battlefield")
    void artifactLosesFlashWhenRaffLeaves() {
        harness.addToBattlefield(player1, new RaffCapashenShipsMage());

        // Remove Raff from battlefield
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Only affects controller =====

    @Test
    @DisplayName("Raff only grants flash to its controller's historic spells")
    void onlyAffectsController() {
        // Player2 controls Raff, player1 should not benefit
        harness.addToBattlefield(player2, new RaffCapashenShipsMage());

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Cannot cast historic without Raff =====

    @Test
    @DisplayName("Artifact spells cannot be cast at instant speed without Raff")
    void cannotCastArtifactAtInstantSpeedWithoutRaff() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
