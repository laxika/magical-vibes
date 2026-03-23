package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllTargetPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GarrukWildspeakerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeAbilities() {
        GarrukWildspeaker card = new GarrukWildspeaker();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has UntapAllTargetPermanentsEffect targeting lands")
    void plusOneAbilityHasCorrectEffects() {
        GarrukWildspeaker card = new GarrukWildspeaker();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(UntapAllTargetPermanentsEffect.class);
        assertThat(ability.getMultiTargetFilters()).hasSize(2);
        assertThat(ability.getMinTargets()).isEqualTo(2);
        assertThat(ability.getMaxTargets()).isEqualTo(2);
    }

    @Test
    @DisplayName("-1 ability creates a Beast token")
    void minusOneAbilityHasCorrectEffect() {
        GarrukWildspeaker card = new GarrukWildspeaker();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-1);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().get(0)).isInstanceOf(CreateTokenEffect.class);
    }

    @Test
    @DisplayName("-4 ability boosts creatures and grants trample")
    void minusFourAbilityHasCorrectEffects() {
        GarrukWildspeaker card = new GarrukWildspeaker();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-4);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new GarrukWildspeaker()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Garruk Wildspeaker"));
        Permanent garruk = bf.stream().filter(p -> p.getCard().getName().equals("Garruk Wildspeaker")).findFirst().orElseThrow();
        assertThat(garruk.getLoyaltyCounters()).isEqualTo(3);
    }

    // ===== +1 ability: Untap two target lands =====

    @Test
    @DisplayName("+1 untaps two target tapped lands")
    void plusOneUntapsTwoTargetLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getLoyaltyCounters()).isEqualTo(4);
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 can target untapped lands (no-op but still gains loyalty)")
    void plusOneCanTargetUntappedLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getLoyaltyCounters()).isEqualTo(4);
        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 can target opponent's lands")
    void plusOneCanTargetOpponentsLands() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent ownForest = addForest(player1);
        Permanent oppForest = addForest(player2);
        ownForest.tap();
        oppForest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(ownForest.getId(), oppForest.getId()));
        harness.passBothPriorities();

        assertThat(garruk.getLoyaltyCounters()).isEqualTo(4);
        assertThat(ownForest.isTapped()).isFalse();
        assertThat(oppForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("+1 can target different land types")
    void plusOneCanTargetDifferentLandTypes() {
        Permanent garruk = addReadyGarruk(player1);
        Permanent forest = addForest(player1);
        Permanent mountain = addMountain(player1);
        forest.tap();
        mountain.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(forest.getId(), mountain.getId()));
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
    }

    // ===== -1 ability: Create a 3/3 green Beast creature token =====

    @Test
    @DisplayName("-1 creates a 3/3 green Beast token")
    void minusOneCreatesBeastToken() {
        Permanent garruk = addReadyGarruk(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(garruk.getLoyaltyCounters()).isEqualTo(2);
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent token = bf.stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.BEAST);
    }

    @Test
    @DisplayName("-1 can be used multiple turns to create multiple tokens")
    void minusOneCreatesMultipleTokensOverTurns() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setLoyaltyCounters(5);

        // First activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getLoyaltyCounters()).isEqualTo(4);

        // Reset for next turn
        garruk.setLoyaltyActivationsThisTurn(0);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Second activation
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getLoyaltyCounters()).isEqualTo(3);
        long tokenCount = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Beast"))
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    // ===== -4 ability: Creatures you control get +3/+3 and gain trample =====

    @Test
    @DisplayName("-4 gives +3/+3 and trample to controlled creatures until end of turn")
    void minusFourBoostsAndGrantsTrample() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setLoyaltyCounters(7);

        // Add a creature
        com.github.laxika.magicalvibes.cards.g.GarruksCompanion companion = new com.github.laxika.magicalvibes.cards.g.GarruksCompanion();
        Permanent creaturePerm = new Permanent(companion);
        creaturePerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(creaturePerm);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(garruk.getLoyaltyCounters()).isEqualTo(3);
        // Garruk's Companion is a 3/2, so +3/+3 = 6/5
        assertThat(creaturePerm.getEffectivePower()).isEqualTo(6);
        assertThat(creaturePerm.getEffectiveToughness()).isEqualTo(5);
        assertThat(creaturePerm.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("-4 does not affect opponent's creatures")
    void minusFourDoesNotAffectOpponentCreatures() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setLoyaltyCounters(7);

        // Add opponent creature (use GrizzlyBears - 2/2 vanilla, no trample)
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears oppCreature = new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        Permanent oppPerm = new Permanent(oppCreature);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(oppPerm);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // GrizzlyBears is 2/2, should be unaffected
        assertThat(oppPerm.getEffectivePower()).isEqualTo(2);
        assertThat(oppPerm.getEffectiveToughness()).isEqualTo(2);
        assertThat(oppPerm.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate -4 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyGarruk(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Garruk dies when -4 brings loyalty to 0 (from starting 4)")
    void minusFourWithFourLoyaltyKillsGarruk() {
        Permanent garruk = addReadyGarruk(player1);
        garruk.setLoyaltyCounters(4);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Garruk Wildspeaker"));
    }

    // ===== Helpers =====

    private Permanent addReadyGarruk(Player player) {
        GarrukWildspeaker card = new GarrukWildspeaker();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addForest(Player player) {
        Forest forest = new Forest();
        Permanent perm = new Permanent(forest);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addMountain(Player player) {
        Mountain mountain = new Mountain();
        Permanent perm = new Permanent(mountain);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
