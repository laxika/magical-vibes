package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AnimateTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrainLifePerControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.s.ShimmerMyr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TezzeretAgentOfBolasTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeAbilities() {
        TezzeretAgentOfBolas card = new TezzeretAgentOfBolas();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability looks at top 5 cards for an artifact")
    void plusOneAbilityHasCorrectEffect() {
        TezzeretAgentOfBolas card = new TezzeretAgentOfBolas();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect.class);
        var effect = (LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect) ability.getEffects().getFirst();
        assertThat(effect.count()).isEqualTo(5);
        assertThat(effect.predicate()).isEqualTo(new CardTypePredicate(CardType.ARTIFACT));
    }

    @Test
    @DisplayName("-1 ability animates target artifact to 5/5 creature")
    void minusOneAbilityHasCorrectEffect() {
        TezzeretAgentOfBolas card = new TezzeretAgentOfBolas();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AnimateTargetPermanentEffect.class);
        var effect = (AnimateTargetPermanentEffect) ability.getEffects().getFirst();
        assertThat(effect.power()).isEqualTo(5);
        assertThat(effect.toughness()).isEqualTo(5);
        assertThat(ability.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        ));
    }

    @Test
    @DisplayName("-4 ability drains life per artifact controlled")
    void minusFourAbilityHasCorrectEffect() {
        TezzeretAgentOfBolas card = new TezzeretAgentOfBolas();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-4);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(DrainLifePerControlledPermanentEffect.class);
        var effect = (DrainLifePerControlledPermanentEffect) ability.getEffects().getFirst();
        assertThat(effect.filter()).isInstanceOf(PermanentIsArtifactPredicate.class);
        assertThat(effect.multiplier()).isEqualTo(2);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new TezzeretAgentOfBolas()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Tezzeret, Agent of Bolas"));
        Permanent tezz = bf.stream().filter(p -> p.getCard().getName().equals("Tezzeret, Agent of Bolas")).findFirst().orElseThrow();
        assertThat(tezz.getLoyaltyCounters()).isEqualTo(3);
    }

    // ===== -1 ability: Animate target artifact =====

    @Test
    @DisplayName("-1 makes target artifact a 5/5 creature")
    void minusOneAnimatesArtifact() {
        Permanent tezz = addReadyTezzeret(player1);
        Permanent solRing = addArtifact(player1);

        harness.activateAbility(player1, 0, 1, null, solRing.getId());
        harness.passBothPriorities();

        assertThat(tezz.getLoyaltyCounters()).isEqualTo(2);
        assertThat(solRing.isPermanentlyAnimated()).isTrue();
        assertThat(solRing.getEffectivePower()).isEqualTo(5);
        assertThat(solRing.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 animation persists across turns (not until end of turn)")
    void minusOneAnimationPersistsAcrossTurns() {
        Permanent tezz = addReadyTezzeret(player1);
        Permanent solRing = addArtifact(player1);

        harness.activateAbility(player1, 0, 1, null, solRing.getId());
        harness.passBothPriorities();

        // Simulate end of turn reset
        solRing.resetModifiers();

        // Animation should persist - it's permanent, not "until end of turn"
        assertThat(solRing.isPermanentlyAnimated()).isTrue();
        assertThat(solRing.getEffectivePower()).isEqualTo(5);
        assertThat(solRing.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 cannot target a non-artifact permanent")
    void minusOneCannotTargetNonArtifact() {
        addReadyTezzeret(player1);
        // Add a creature (not an artifact)
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Permanent bearsPerm = new Permanent(bears);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bearsPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bearsPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    @Test
    @DisplayName("-1 animated artifact retains artifact type")
    void minusOneAnimatedArtifactRetainsArtifactType() {
        addReadyTezzeret(player1);
        Permanent solRing = addArtifact(player1);

        harness.activateAbility(player1, 0, 1, null, solRing.getId());
        harness.passBothPriorities();

        // Should still be an artifact
        assertThat(solRing.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        // And now also a creature via permanent animation
        assertThat(solRing.isPermanentlyAnimated()).isTrue();
    }

    @Test
    @DisplayName("-1 on equipped Equipment unattaches it (CR 301.5c)")
    void minusOneOnEquipmentUnattachesIt() {
        addReadyTezzeret(player1);
        // Add a creature and equip it
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Permanent bearsPerm = new Permanent(bears);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bearsPerm);

        com.github.laxika.magicalvibes.cards.s.SwordOfFeastAndFamine sword = new com.github.laxika.magicalvibes.cards.s.SwordOfFeastAndFamine();
        Permanent swordPerm = new Permanent(sword);
        swordPerm.setAttachedTo(bearsPerm.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(swordPerm);

        // Animate the equipment
        harness.activateAbility(player1, 0, 1, null, swordPerm.getId());
        harness.passBothPriorities();

        // Equipment should be unattached and animated
        assertThat(swordPerm.getAttachedTo()).isNull();
        assertThat(swordPerm.isPermanentlyAnimated()).isTrue();
        assertThat(swordPerm.getEffectivePower()).isEqualTo(5);
        assertThat(swordPerm.getEffectiveToughness()).isEqualTo(5);
    }

    // ===== -4 ability: Drain life =====

    @Test
    @DisplayName("-4 drains target player for twice artifact count")
    void minusFourDrainsForTwiceArtifactCount() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setLoyaltyCounters(4);
        addArtifact(player1);
        addArtifact(player1);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 3 artifacts × 2 = 6 life drained
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(26);
        assertThat(tezz.getLoyaltyCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("-4 drains zero with no artifacts")
    void minusFourDrainsZeroWithNoArtifacts() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setLoyaltyCounters(4);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // No artifacts = 0 drain
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-4 does not count opponent's artifacts")
    void minusFourDoesNotCountOpponentArtifacts() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setLoyaltyCounters(4);
        addArtifact(player1);
        addArtifact(player2);
        addArtifact(player2);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Only 1 artifact controlled by player1 × 2 = 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("-4 can target self")
    void minusFourCanTargetSelf() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setLoyaltyCounters(4);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // 1 artifact × 2 = 2. Player1 loses 2 then gains 2 = net 0
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate -4 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyTezzeret(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Tezzeret dies when -4 brings loyalty to 0 but ability still resolves")
    void minusFourKillsTezzeretButAbilityResolves() {
        Permanent tezz = addReadyTezzeret(player1);
        tezz.setLoyaltyCounters(4);
        addArtifact(player1);
        addArtifact(player1);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Tezzeret should be dead (0 loyalty)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Tezzeret, Agent of Bolas"));
        // But ability still resolved: 2 artifacts × 2 = 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    // ===== Helpers =====

    private Permanent addReadyTezzeret(Player player) {
        TezzeretAgentOfBolas card = new TezzeretAgentOfBolas();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addArtifact(Player player) {
        ShimmerMyr shimmerMyr = new ShimmerMyr();
        Permanent perm = new Permanent(shimmerMyr);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
