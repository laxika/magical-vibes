package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagmaPhoenix;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromGraveyardsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshesOfTheAbhorrentTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has STATIC effects: PlayersCantCastSpellsFromGraveyardsEffect and PlayersCantActivateAbilitiesOfGraveyardCardsEffect")
    void hasStaticEffects() {
        AshesOfTheAbhorrent card = new AshesOfTheAbhorrent();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PlayersCantCastSpellsFromGraveyardsEffect);
        assertThat(card.getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof PlayersCantActivateAbilitiesOfGraveyardCardsEffect);
    }

    @Test
    @DisplayName("Has ON_ANY_CREATURE_DIES effect: GainLifeEffect(1)")
    void hasDeathTriggerEffect() {
        AshesOfTheAbhorrent card = new AshesOfTheAbhorrent();

        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst())
                .isInstanceOf(GainLifeEffect.class);
        GainLifeEffect gainLife = (GainLifeEffect) card.getEffects(EffectSlot.ON_ANY_CREATURE_DIES).getFirst();
        assertThat(gainLife.amount()).isEqualTo(1);
    }

    // ===== Life gain when creatures die =====

    @Test
    @DisplayName("Controller gains 1 life when an opponent's creature dies")
    void gainsLifeWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill opponent's creature with Shock
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve life gain trigger

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller gains 1 life when own creature dies")
    void gainsLifeWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Kill own creature with opponent's Shock
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities(); // Resolve Shock → bears die → death trigger
        harness.passBothPriorities(); // Resolve life gain trigger

        harness.assertLife(player1, 21);
    }

    // ===== Graveyard spell casting prevention =====

    @Test
    @DisplayName("Prevents flashback casting when Ashes is on the battlefield")
    void preventsFlashbackCasting() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        // Player 2 should not be able to cast flashback
        setupPlayer2Active();
        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents controller from casting flashback too")
    void preventsControllerFlashbackCasting() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Controller also can't cast flashback (it affects all players)
        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback indices are empty when Ashes is on the battlefield")
    void flashbackIndicesAreEmpty() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        setupPlayer2Active();
        List<Integer> playable = harness.getGameBroadcastService()
                .getPlayableFlashbackIndices(gd, player2.getId());
        assertThat(playable).isEmpty();
    }

    // ===== Graveyard ability activation prevention =====

    @Test
    @DisplayName("Prevents graveyard activated abilities when Ashes is on the battlefield")
    void preventsGraveyardAbilityActivation() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player2, List.of(new MagmaPhoenix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        setupPlayer2Active();
        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents controller from activating graveyard abilities too")
    void preventsControllerGraveyardAbilityActivation() {
        harness.addToBattlefield(player1, new AshesOfTheAbhorrent());
        harness.setGraveyard(player1, List.of(new MagmaPhoenix()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
