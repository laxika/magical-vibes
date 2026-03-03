package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GlintHawkIdol;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorworksTest extends BaseCardTest {

    @Test
    @DisplayName("Mirrorworks has correct effect structure")
    void hasCorrectEffectStructure() {
        Mirrorworks card = new Mirrorworks();

        var effects = card.getEffects(EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(MayPayManaEffect.class);

        MayPayManaEffect mayPay = (MayPayManaEffect) effects.getFirst();
        assertThat(mayPay.manaCost()).isEqualTo("{2}");
        assertThat(mayPay.wrapped()).isInstanceOf(CreateTokenCopyOfTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Casting another artifact triggers may-pay ability")
    void castingAnotherArtifactTriggersMayPayAbility() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4); // 2 for Glint Hawk Idol + 2 for may-pay

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell, artifact enters, trigger fires

        // Should be prompted with may ability to pay {2}
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Paying {2} creates a token copy of the entering artifact")
    void payingCreatesTokenCopy() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4); // 2 for Glint Hawk Idol + 2 for may-pay

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell, trigger fires

        // Accept and pay {2}
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the token copy effect

        // Should have Mirrorworks + original Glint Hawk Idol + token copy = 3 permanents
        long idolCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol"))
                .count();
        assertThat(idolCount).isEqualTo(2);

        // One of them should be a token
        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining does not create a token")
    void decliningDoesNotCreateToken() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // just enough for Glint Hawk Idol

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell, trigger fires

        // Decline
        harness.handleMayAbilityChosen(player1, false);

        // Should have only Mirrorworks + original Glint Hawk Idol
        long idolCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol"))
                .count();
        assertThat(idolCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot pay if not enough mana")
    void cannotPayIfNotEnoughMana() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // just enough for Glint Hawk Idol, none for may-pay

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell, trigger fires

        // Try to accept but can't afford it
        harness.handleMayAbilityChosen(player1, true);

        // No token should be created
        long idolCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol"))
                .count();
        assertThat(idolCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy entering does not trigger Mirrorworks again (nontoken)")
    void tokenCopyDoesNotRetrigger() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6); // 2 for idol + 2 for may-pay + 2 extra

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell, trigger fires

        // Accept and pay {2} to create token copy
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the token copy effect

        // The token entering should NOT trigger Mirrorworks again (nontoken restriction)
        // Check that we don't have another may ability prompt
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger on itself entering")
    void doesNotTriggerOnItself() {
        harness.setHand(player1, List.of(new Mirrorworks()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve Mirrorworks spell

        // Should not prompt for may ability (no other artifact with the trigger)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Token copy has same abilities as the original")
    void tokenCopyHasSameAbilities() {
        addMirrorworksReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact spell, trigger fires

        // Accept and pay {2}
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the token copy effect

        // Find the token copy
        Permanent tokenCopy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Glint Hawk Idol") && p.getCard().isToken())
                .findFirst().orElseThrow();

        // Token should have the same activated ability as the original
        assertThat(tokenCopy.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(tokenCopy.getCard().getActivatedAbilities().get(0).getManaCost()).isEqualTo("{W}");

        // Token should have the same triggered ability
        var triggerEffects = tokenCopy.getCard().getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
        assertThat(triggerEffects).hasSize(1);
    }

    private Permanent addMirrorworksReady(Player player) {
        Mirrorworks card = new Mirrorworks();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
