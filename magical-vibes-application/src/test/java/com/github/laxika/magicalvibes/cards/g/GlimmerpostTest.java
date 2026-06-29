package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeForEachSubtypeOnBattlefieldEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlimmerpostTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ETB gain life for each Locus effect")
    void hasEtbGainLifeEffect() {
        Glimmerpost card = new Glimmerpost();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(GainLifeForEachSubtypeOnBattlefieldEffect.class);

        GainLifeForEachSubtypeOnBattlefieldEffect effect =
                (GainLifeForEachSubtypeOnBattlefieldEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.LOCUS);
    }

    @Test
    @DisplayName("Has tap for colorless mana ability")
    void hasTapForColorlessManaAbility() {
        Glimmerpost card = new Glimmerpost();

        assertThat(card.getEffects(EffectSlot.ON_TAP)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_TAP).getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.COLORLESS));
    }

    // ===== ETB trigger =====

    @Test
    @DisplayName("Playing Glimmerpost puts ETB trigger on the stack")
    void playingPutsEtbTriggerOnStack() {
        playGlimmerpost(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Glimmerpost");
    }

    @Test
    @DisplayName("ETB gains 1 life when Glimmerpost is the only Locus")
    void etbGainsOneLifeWithSingleLocus() {
        harness.setLife(player1, 20);
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("ETB gains 2 life when a second Locus is already on the battlefield")
    void etbGainsTwoLifeWithTwoLoci() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB counts Loci controlled by all players")
    void etbCountsAllPlayersLoci() {
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        // Counts opponent's Locus + own Glimmerpost = 2
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("ETB gains 3 life with three Loci on the battlefield")
    void etbGainsThreeLifeWithThreeLoci() {
        harness.addToBattlefield(player1, new Glimmerpost());
        harness.addToBattlefield(player2, new Glimmerpost());
        harness.setLife(player1, 20);

        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Game log records life gain from ETB")
    void gameLogRecordsLifeGain() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.gameLog).anyMatch(log -> log.contains("gains 1 life"));
    }

    // ===== Land enters battlefield =====

    @Test
    @DisplayName("Glimmerpost enters the battlefield as a permanent")
    void entersBattlefieldAsPermanent() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        harness.assertOnBattlefield(player1, "Glimmerpost");
    }

    @Test
    @DisplayName("Stack is empty after ETB fully resolves")
    void stackEmptyAfterResolution() {
        playGlimmerpost(player1);
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void playGlimmerpost(Player player) {
        harness.setHand(player, List.of(new Glimmerpost()));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player, 0);
    }
}
