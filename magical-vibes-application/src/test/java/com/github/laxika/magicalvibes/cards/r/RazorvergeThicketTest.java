package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessFewLandsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RazorvergeThicketTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Razorverge Thicket has conditional enters-tapped effect")
    void hasConditionalEntersTappedEffect() {
        RazorvergeThicket card = new RazorvergeThicket();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof EntersTappedUnlessFewLandsEffect)
                .hasSize(1);
        EntersTappedUnlessFewLandsEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof EntersTappedUnlessFewLandsEffect)
                .map(e -> (EntersTappedUnlessFewLandsEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.maxOtherLands()).isEqualTo(2);
    }

    @Test
    @DisplayName("Razorverge Thicket has two mana abilities for green and white")
    void hasManaAbilities() {
        RazorvergeThicket card = new RazorvergeThicket();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: {T}: Add {G}.
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.GREEN));

        // Second ability: {T}: Add {W}.
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.WHITE));
    }

    // ===== Enters the battlefield: untapped (few lands) =====

    @Test
    @DisplayName("Enters untapped when you control zero other lands")
    void entersUntappedWithZeroLands() {
        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control one other land")
    void entersUntappedWithOneLand() {
        addBasicLand(player1);

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control exactly two other lands")
    void entersUntappedWithTwoLands() {
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    // ===== Enters the battlefield: tapped (too many lands) =====

    @Test
    @DisplayName("Enters tapped when you control three other lands")
    void entersTappedWithThreeLands() {
        addBasicLand(player1);
        addBasicLand(player1);
        addBasicLand(player1);

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you control many other lands")
    void entersTappedWithManyLands() {
        for (int i = 0; i < 5; i++) {
            addBasicLand(player1);
        }

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isTrue();
    }

    // ===== Only counts lands, not other permanents =====

    @Test
    @DisplayName("Non-land permanents do not count toward the land check")
    void nonLandPermanentsDoNotCount() {
        // Add 3 creatures (not lands)
        for (int i = 0; i < 3; i++) {
            Permanent creature = new Permanent(new LlanowarElves());
            gd.playerBattlefields.get(player1.getId()).add(creature);
        }

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        // 0 lands, 3 creatures — should enter untapped
        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    // ===== Only counts your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's lands do not count toward the land check")
    void opponentLandsDoNotCount() {
        // Give opponent 5 lands
        for (int i = 0; i < 5; i++) {
            addBasicLand(player2);
        }

        harness.setHand(player1, List.of(new RazorvergeThicket()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        // Player1 has 0 other lands — should enter untapped
        Permanent thicket = findThicket(player1);
        assertThat(thicket.isTapped()).isFalse();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addThicketReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addThicketReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addThicketReady(Player player) {
        Permanent perm = new Permanent(new RazorvergeThicket());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addBasicLand(Player player) {
        // Create a simple land permanent — use a card that's typed as LAND
        com.github.laxika.magicalvibes.model.Card land = new com.github.laxika.magicalvibes.cards.m.Mountain();
        Permanent perm = new Permanent(land);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private Permanent findThicket(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Razorverge Thicket"))
                .findFirst().orElseThrow();
    }
}
