package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
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

class TreetopVillageTest {

    private GameTestHarness harness;
    private Player player1;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Treetop Village has correct card properties")
    void hasCorrectProperties() {
        TreetopVillage card = new TreetopVillage();

        assertThat(card.getName()).isEqualTo("Treetop Village");
        assertThat(card.getType()).isEqualTo(CardType.LAND);
        assertThat(card.isEntersTapped()).isTrue();
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{G}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(AnimateLandEffect.class);
    }

    // ===== Enters the battlefield tapped =====

    @Test
    @DisplayName("Treetop Village enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new TreetopVillage()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent village = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Treetop Village"))
                .findFirst().orElseThrow();
        assertThat(village.isTapped()).isTrue();
    }

    // ===== Tap for mana =====

    @Test
    @DisplayName("Tapping Treetop Village produces green mana")
    void tappingProducesGreenMana() {
        Permanent village = addVillageReady(player1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(village);

        gs.tapPermanent(gd, player1, index);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    // ===== Animate ability =====

    @Test
    @DisplayName("Activating ability puts AnimateLand on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Treetop Village");
        assertThat(entry.getTargetPermanentId()).isEqualTo(village.getId());
    }

    @Test
    @DisplayName("Resolving ability makes it a 3/3 green Ape creature with trample")
    void resolvingAbilityMakesItA3x3WithTrample() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(village.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(village.getAnimatedPower()).isEqualTo(3);
        assertThat(village.getAnimatedToughness()).isEqualTo(3);
        assertThat(gqs.isCreature(gd, village)).isTrue();
        assertThat(gqs.getEffectivePower(gd, village)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, village)).isEqualTo(3);
        assertThat(village.getAnimatedColor()).isEqualTo(CardColor.GREEN);
        assertThat(village.getGrantedSubtypes()).containsExactly(CardSubtype.APE);
        assertThat(village.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Treetop Village is still a land while animated")
    void stillALandWhileAnimated() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(village.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(gqs.isCreature(gd, village)).isTrue();
    }

    @Test
    @DisplayName("Activating ability does NOT tap the permanent")
    void activatingAbilityDoesNotTap() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(village.isTapped()).isFalse();
    }

    // ===== Animation resets at end of turn =====

    @Test
    @DisplayName("Animation resets at end of turn")
    void animationResetsAtEndOfTurn() {
        Permanent village = addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, village)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(village.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, village)).isFalse();
        assertThat(village.getGrantedKeywords()).isEmpty();
        assertThat(village.getGrantedSubtypes()).isEmpty();
        assertThat(village.getAnimatedColor()).isNull();
    }

    // ===== Mana cost enforcement =====

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addVillageReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    // ===== Not a creature before activation =====

    @Test
    @DisplayName("Treetop Village is not a creature before activation")
    void notACreatureBeforeActivation() {
        Permanent village = addVillageReady(player1);

        assertThat(gqs.isCreature(gd, village)).isFalse();
        assertThat(village.getCard().getType()).isEqualTo(CardType.LAND);
    }

    // ===== Helper methods =====

    private Permanent addVillageReady(Player player) {
        TreetopVillage card = new TreetopVillage();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
