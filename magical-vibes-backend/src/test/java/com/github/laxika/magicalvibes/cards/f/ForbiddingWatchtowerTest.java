package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddingWatchtowerTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Forbidding Watchtower has correct card properties")
    void hasCorrectProperties() {
        ForbiddingWatchtower card = new ForbiddingWatchtower();

        assertThat(card.getName()).isEqualTo("Forbidding Watchtower");
        assertThat(card.getType()).isEqualTo(CardType.LAND);
        assertThat(card.getManaCost()).isNull();
        assertThat(card.isEntersTapped()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{W}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateLandEffect.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Forbidding Watchtower enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new ForbiddingWatchtower()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent watchtower = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forbidding Watchtower"))
                .findFirst().orElseThrow();
        assertThat(watchtower.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Forbidding Watchtower produces white mana")
    void tappingProducesWhiteMana() {
        Permanent watchtower = addWatchtowerReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(watchtower);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Forbidding Watchtower");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(watchtower.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 1/5 creature")
    void resolvingAbilityMakesItA1x5() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(watchtower.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, watchtower)).isTrue();
        assertThat(gqs.getEffectivePower(gd, watchtower)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watchtower)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animated Forbidding Watchtower gains Soldier subtype")
    void animatedGainsSoldierSubtype() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchtower.getGrantedSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Animated Forbidding Watchtower becomes white")
    void animatedBecomesWhite() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchtower.getAnimatedColor()).isEqualTo(CardColor.WHITE);
    }

    @Test
    @DisplayName("Animated Forbidding Watchtower does not have flying")
    void animatedDoesNotHaveFlying() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchtower.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Forbidding Watchtower is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(watchtower.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(gqs.isCreature(gd, watchtower)).isTrue();
    }

    // ===== End of turn resets animation =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent watchtower = addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, watchtower)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(watchtower.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, watchtower)).isFalse();
        assertThat(watchtower.getGrantedSubtypes()).isEmpty();
        assertThat(watchtower.getAnimatedColor()).isNull();
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Forbidding Watchtower is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent watchtower = addWatchtowerReady(player1);

        assertThat(gqs.isCreature(gd, watchtower)).isFalse();
    }

    // ===== Ability fizzles if removed =====

    @Test
    @DisplayName("Ability fizzles if Forbidding Watchtower is removed before resolution")
    void abilityFizzlesIfSourceRemoved() {
        addWatchtowerReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addWatchtowerReady(Player player) {
        ForbiddingWatchtower card = new ForbiddingWatchtower();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
