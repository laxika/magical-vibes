package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

class HinterlandHarborTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has conditional enters-tapped effect checking for Forest or Island")
    void hasConditionalEntersTappedEffect() {
        HinterlandHarbor card = new HinterlandHarbor();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .hasSize(1);
        EntersTappedUnlessControlLandSubtypeEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .map(e -> (EntersTappedUnlessControlLandSubtypeEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.requiredSubtypes()).containsExactlyInAnyOrder(CardSubtype.FOREST, CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Has two mana abilities for green and blue")
    void hasManaAbilities() {
        HinterlandHarbor card = new HinterlandHarbor();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.GREEN));

        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.BLUE));
    }

    // ===== Enters tapped (no qualifying lands) =====

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Mountain)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Mountain());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isTrue();
    }

    // ===== Enters untapped (qualifying lands present) =====

    @Test
    @DisplayName("Enters untapped when you control a Forest")
    void entersUntappedWithForest() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both a Forest and an Island")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isFalse();
    }

    // ===== Only checks your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's Forest does not satisfy the check")
    void opponentForestDoesNotCount() {
        harness.addToBattlefield(player2, new Forest());

        harness.setHand(player1, List.of(new HinterlandHarbor()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent harbor = findHarbor(player1);
        assertThat(harbor.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        addHarborReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addHarborReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addHarborReady(Player player) {
        Permanent perm = new Permanent(new HinterlandHarbor());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findHarbor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hinterland Harbor"))
                .findFirst().orElseThrow();
    }
}
