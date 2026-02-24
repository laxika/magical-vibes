package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MetalcraftConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EtchedChampionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Etched Champion has metalcraft conditional protection from all colors")
    void hasCorrectProperties() {
        EtchedChampion card = new EtchedChampion();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MetalcraftConditionalEffect.class);

        MetalcraftConditionalEffect metalcraft = (MetalcraftConditionalEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(metalcraft.wrapped()).isInstanceOf(ProtectionFromColorsEffect.class);

        ProtectionFromColorsEffect protection = (ProtectionFromColorsEffect) metalcraft.wrapped();
        assertThat(protection.colors()).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN
        );
    }

    // ===== Metalcraft active: protection from all colors =====

    @Test
    @DisplayName("With metalcraft, Etched Champion has protection from all colors")
    void hasProtectionWithMetalcraft() {
        harness.addToBattlefield(player1, new EtchedChampion());
        // Etched Champion itself is an artifact; add two more for metalcraft
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Etched Champion"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.GREEN)).isTrue();
    }

    // ===== Metalcraft inactive: no protection =====

    @Test
    @DisplayName("Without metalcraft, Etched Champion has no protection")
    void noProtectionWithoutMetalcraft() {
        harness.addToBattlefield(player1, new EtchedChampion());
        // Only one artifact (itself), not enough for metalcraft

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Etched Champion"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("With only two artifacts, Etched Champion has no protection")
    void noProtectionWithTwoArtifacts() {
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new LeoninScimitar());
        // Two artifacts, still not enough

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Etched Champion"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.BLACK)).isFalse();
    }

    // ===== Protection prevents blocking by colored creatures =====

    @Test
    @DisplayName("With metalcraft, colored creature cannot block Etched Champion")
    void coloredCreatureCannotBlockWithMetalcraft() {
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent champion = new Permanent(new EtchedChampion());
        champion.setSummoningSick(false);
        champion.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(champion);

        int championIdx = gd.playerBattlefields.get(player1.getId()).indexOf(champion);

        // Green blocker
        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, championIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    // ===== Without metalcraft, colored creatures can block =====

    @Test
    @DisplayName("Without metalcraft, colored creature can block Etched Champion")
    void coloredCreatureCanBlockWithoutMetalcraft() {
        // Only one artifact (itself), no metalcraft
        Permanent champion = new Permanent(new EtchedChampion());
        champion.setSummoningSick(false);
        champion.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(champion);

        GrizzlyBears blocker = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(blocker);
        blockerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blockerPerm.isBlocking()).isTrue();
    }

    // ===== Protection lost when artifact count drops =====

    @Test
    @DisplayName("Protection is lost when artifact count drops below three")
    void protectionLostWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new EtchedChampion());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent champion = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Etched Champion"))
                .findFirst().orElseThrow();

        // With 3 artifacts, has protection
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isTrue();

        // Remove one artifact
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Leonin Scimitar"));

        // Now only 1 artifact, no protection
        assertThat(gqs.hasProtectionFrom(gd, champion, CardColor.RED)).isFalse();
    }
}
