package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateSelfWithStatsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlintHawkIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Glint Hawk Idol has correct card properties")
    void hasCorrectProperties() {
        GlintHawkIdol card = new GlintHawkIdol();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{W}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateSelfWithStatsEffect.class);

        var triggerEffects = card.getEffects(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD);
        assertThat(triggerEffects).hasSize(1);
        assertThat(triggerEffects.getFirst()).isInstanceOf(MayEffect.class);
    }

    @Test
    @DisplayName("Casting Glint Hawk Idol puts it on the battlefield")
    void castingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glint Hawk Idol"));
    }

    @Test
    @DisplayName("Glint Hawk Idol is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent idolPerm = addIdolReady(player1);

        assertThat(gqs.isCreature(gd, idolPerm)).isFalse();
        assertThat(idolPerm.getCard().getType()).isEqualTo(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Activating {W} ability animates Idol as 2/2 Bird with flying")
    void activatingAbilityAnimatesIdol() {
        Permanent idolPerm = addIdolReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(idolPerm.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(idolPerm.getAnimatedPower()).isEqualTo(2);
        assertThat(idolPerm.getAnimatedToughness()).isEqualTo(2);
        assertThat(gqs.isCreature(gd, idolPerm)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idolPerm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, idolPerm)).isEqualTo(2);
        assertThat(idolPerm.getGrantedSubtypes()).contains(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, idolPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        addIdolReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        Permanent idol = findPermanent(player1, "Glint Hawk Idol");
        assertThat(idol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent idolPerm = addIdolReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, idolPerm)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(idolPerm.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idolPerm)).isFalse();
        assertThat(idolPerm.getGrantedSubtypes()).isEmpty();
        assertThat(idolPerm.getGrantedKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Triggered ability fires when another artifact enters")
    void triggeredAbilityFiresOnArtifactEnter() {
        addIdolReady(player1);

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting trigger animates Idol")
    void acceptingTriggerAnimatesIdol() {
        Permanent idolPerm = addIdolReady(player1);

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(idolPerm.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(idolPerm.getAnimatedPower()).isEqualTo(2);
        assertThat(idolPerm.getAnimatedToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, idolPerm, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Declining trigger does not animate Idol")
    void decliningTriggerDoesNotAnimateIdol() {
        Permanent idolPerm = addIdolReady(player1);

        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(idolPerm.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idolPerm)).isFalse();
    }

    @Test
    @DisplayName("Trigger does not fire on itself")
    void triggerDoesNotFireOnItself() {
        harness.setHand(player1, List.of(new GlintHawkIdol()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isNull();
    }

    @Test
    @DisplayName("Can activate ability multiple times per turn")
    void canActivateMultipleTimesPerTurn() {
        Permanent idolPerm = addIdolReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, idolPerm)).isTrue();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, idolPerm)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idolPerm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability requires {W} mana")
    void activatedAbilityRequiresMana() {
        addIdolReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> harness.activateAbility(player1, 0, null, null)
        );
    }

    private Permanent addIdolReady(Player player) {
        GlintHawkIdol card = new GlintHawkIdol();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
