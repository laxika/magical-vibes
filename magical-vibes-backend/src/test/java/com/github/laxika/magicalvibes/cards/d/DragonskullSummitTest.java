package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedUnlessControlLandSubtypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonskullSummitTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has conditional enters-tapped effect checking for Swamp or Mountain")
    void hasConditionalEntersTappedEffect() {
        DragonskullSummit card = new DragonskullSummit();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .hasSize(1);
        EntersTappedUnlessControlLandSubtypeEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .map(e -> (EntersTappedUnlessControlLandSubtypeEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.requiredSubtypes()).containsExactlyInAnyOrder(CardSubtype.SWAMP, CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Has two mana abilities for black and red")
    void hasManaAbilities() {
        DragonskullSummit card = new DragonskullSummit();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.BLACK));

        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.RED));
    }

    // ===== Enters tapped (no qualifying lands) =====

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Forest)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isTrue();
    }

    // ===== Enters untapped (qualifying lands present) =====

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Mountain")
    void entersUntappedWithMountain() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Swamp and a Mountain")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isFalse();
    }

    // ===== Only checks your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's Swamp does not satisfy the check")
    void opponentSwampDoesNotCount() {
        harness.addToBattlefield(player2, new Swamp());

        harness.setHand(player1, List.of(new DragonskullSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent summit = findSummit(player1);
        assertThat(summit.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addSummitReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        addSummitReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addSummitReady(Player player) {
        Permanent perm = new Permanent(new DragonskullSummit());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findSummit(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragonskull Summit"))
                .findFirst().orElseThrow();
    }
}
