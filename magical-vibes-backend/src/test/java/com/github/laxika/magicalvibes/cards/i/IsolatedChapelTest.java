package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class IsolatedChapelTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has conditional enters-tapped effect checking for Plains or Swamp")
    void hasConditionalEntersTappedEffect() {
        IsolatedChapel card = new IsolatedChapel();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .hasSize(1);
        EntersTappedUnlessControlLandSubtypeEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .map(e -> (EntersTappedUnlessControlLandSubtypeEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.requiredSubtypes()).containsExactlyInAnyOrder(CardSubtype.PLAINS, CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Has two mana abilities for white and black")
    void hasManaAbilities() {
        IsolatedChapel card = new IsolatedChapel();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.WHITE));

        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.BLACK));
    }

    // ===== Enters tapped (no qualifying lands) =====

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Mountain)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isTrue();
    }

    // ===== Enters untapped (qualifying lands present) =====

    @Test
    @DisplayName("Enters untapped when you control a Plains")
    void entersUntappedWithPlains() {
        harness.addToBattlefield(player1, new Plains());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Plains and a Swamp")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isFalse();
    }

    // ===== Only checks your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's Plains does not satisfy the check")
    void opponentPlainsDoesNotCount() {
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new IsolatedChapel()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent chapel = findChapel(player1);
        assertThat(chapel.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for white mana produces one white")
    void tappingProducesWhiteMana() {
        addChapelReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addChapelReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addChapelReady(Player player) {
        Permanent perm = new Permanent(new IsolatedChapel());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findChapel(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Isolated Chapel"))
                .findFirst().orElseThrow();
    }
}
