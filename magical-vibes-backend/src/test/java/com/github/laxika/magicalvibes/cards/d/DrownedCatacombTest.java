package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
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

class DrownedCatacombTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has conditional enters-tapped effect checking for Island or Swamp")
    void hasConditionalEntersTappedEffect() {
        DrownedCatacomb card = new DrownedCatacomb();

        assertThat(card.getEffects(EffectSlot.STATIC))
                .filteredOn(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .hasSize(1);
        EntersTappedUnlessControlLandSubtypeEffect effect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof EntersTappedUnlessControlLandSubtypeEffect)
                .map(e -> (EntersTappedUnlessControlLandSubtypeEffect) e)
                .findFirst().orElseThrow();
        assertThat(effect.requiredSubtypes()).containsExactlyInAnyOrder(CardSubtype.ISLAND, CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Has two mana abilities for blue and black")
    void hasManaAbilities() {
        DrownedCatacomb card = new DrownedCatacomb();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.BLUE));

        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(1).getEffects().getFirst())
                .isEqualTo(new AwardManaEffect(ManaColor.BLACK));
    }

    // ===== Enters tapped (no qualifying lands) =====

    @Test
    @DisplayName("Enters tapped when you control no lands")
    void entersTappedWithNoLands() {
        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when you only control non-matching lands (Forest)")
    void entersTappedWithNonMatchingLands() {
        harness.addToBattlefield(player1, new Forest());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isTrue();
    }

    // ===== Enters untapped (qualifying lands present) =====

    @Test
    @DisplayName("Enters untapped when you control an Island")
    void entersUntappedWithIsland() {
        harness.addToBattlefield(player1, new Island());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control a Swamp")
    void entersUntappedWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enters untapped when you control both an Island and a Swamp")
    void entersUntappedWithBoth() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isFalse();
    }

    // ===== Only checks your lands, not opponent's =====

    @Test
    @DisplayName("Opponent's Island does not satisfy the check")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player2, new Island());

        harness.setHand(player1, List.of(new DrownedCatacomb()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);

        Permanent catacomb = findCatacomb(player1);
        assertThat(catacomb.isTapped()).isTrue();
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        addCatacombReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        addCatacombReady(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    // ===== Helpers =====

    private Permanent addCatacombReady(Player player) {
        Permanent perm = new Permanent(new DrownedCatacomb());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findCatacomb(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Drowned Catacomb"))
                .findFirst().orElseThrow();
    }
}
