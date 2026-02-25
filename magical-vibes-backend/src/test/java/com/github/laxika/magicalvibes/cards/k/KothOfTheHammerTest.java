package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.effect.AddManaPerControlledSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.AnimateLandEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KothEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KothOfTheHammerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has three loyalty abilities")
    void hasThreeAbilities() {
        KothOfTheHammer card = new KothOfTheHammer();
        assertThat(card.getActivatedAbilities()).hasSize(3);
    }

    @Test
    @DisplayName("+1 ability has UntapTargetPermanentEffect and AnimateLandEffect targeting Mountains")
    void plusOneAbilityHasCorrectEffects() {
        KothOfTheHammer card = new KothOfTheHammer();
        var ability = card.getActivatedAbilities().get(0);

        assertThat(ability.getLoyaltyCost()).isEqualTo(1);
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(AnimateLandEffect.class);
        assertThat(ability.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
                "Target must be a Mountain"
        ));
    }

    @Test
    @DisplayName("-2 ability adds mana per controlled Mountain")
    void minusTwoAbilityHasCorrectEffect() {
        KothOfTheHammer card = new KothOfTheHammer();
        var ability = card.getActivatedAbilities().get(1);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-2);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(AddManaPerControlledSubtypeEffect.class);
    }

    @Test
    @DisplayName("-5 ability is the emblem effect")
    void minusFiveAbilityHasCorrectEffect() {
        KothOfTheHammer card = new KothOfTheHammer();
        var ability = card.getActivatedAbilities().get(2);

        assertThat(ability.getLoyaltyCost()).isEqualTo(-5);
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(KothEmblemEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with 3 loyalty")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new KothOfTheHammer()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).anyMatch(p -> p.getCard().getName().equals("Koth of the Hammer"));
        Permanent koth = bf.stream().filter(p -> p.getCard().getName().equals("Koth of the Hammer")).findFirst().orElseThrow();
        assertThat(koth.getLoyaltyCounters()).isEqualTo(3);
    }

    // ===== +1 ability: Untap target Mountain, make it a 4/4 =====

    @Test
    @DisplayName("+1 untaps target Mountain and makes it a 4/4 red Elemental creature")
    void plusOneUntapsAndAnimatesMountain() {
        Permanent koth = addReadyKoth(player1);
        Permanent mountain = addMountain(player1);
        mountain.tap();

        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(koth.getLoyaltyCounters()).isEqualTo(4);
        assertThat(mountain.isTapped()).isFalse();
        assertThat(mountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(mountain.getAnimatedPower()).isEqualTo(4);
        assertThat(mountain.getAnimatedToughness()).isEqualTo(4);
        assertThat(mountain.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("+1 can target an untapped Mountain (still animates it)")
    void plusOneCanTargetUntappedMountain() {
        addReadyKoth(player1);
        Permanent mountain = addMountain(player1);

        harness.activateAbility(player1, 0, 0, null, mountain.getId());
        harness.passBothPriorities();

        assertThat(mountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(mountain.getAnimatedPower()).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 cannot target a non-Mountain land")
    void plusOneCannotTargetNonMountain() {
        addReadyKoth(player1);
        // Add a Forest (not a Mountain)
        com.github.laxika.magicalvibes.cards.f.Forest forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        Permanent forestPerm = new Permanent(forest);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(forestPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forestPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Mountain");
    }

    @Test
    @DisplayName("+1 can target opponent's Mountain")
    void plusOneCanTargetOpponentsMountain() {
        addReadyKoth(player1);
        Permanent opponentMountain = addMountain(player2);
        opponentMountain.tap();

        harness.activateAbility(player1, 0, 0, null, opponentMountain.getId());
        harness.passBothPriorities();

        assertThat(opponentMountain.isTapped()).isFalse();
        assertThat(opponentMountain.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(opponentMountain.getAnimatedPower()).isEqualTo(4);
    }

    // ===== -2 ability: Add {R} for each Mountain you control =====

    @Test
    @DisplayName("-2 adds red mana for each Mountain controlled")
    void minusTwoAddsManaPerMountain() {
        Permanent koth = addReadyKoth(player1);
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(koth.getLoyaltyCounters()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("-2 adds zero mana when controlling no Mountains")
    void minusTwoAddsZeroManaWithNoMountains() {
        addReadyKoth(player1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("-2 does not count opponent's Mountains")
    void minusTwoDoesNotCountOpponentMountains() {
        addReadyKoth(player1);
        addMountain(player1);
        addMountain(player2);
        addMountain(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    // ===== -5 ability: Emblem =====

    @Test
    @DisplayName("-5 creates an emblem")
    void minusFiveCreatesEmblem() {
        Permanent koth = addReadyKoth(player1);
        koth.setLoyaltyCounters(5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Emblem contains GrantActivatedAbilityEffect for Mountains with OWN_PERMANENTS scope")
    void emblemContainsCorrectEffect() {
        Permanent koth = addReadyKoth(player1);
        koth.setLoyaltyCounters(5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).hasSize(1);
        assertThat(emblem.staticEffects().getFirst()).isInstanceOf(GrantActivatedAbilityEffect.class);
        GrantActivatedAbilityEffect grant = (GrantActivatedAbilityEffect) emblem.staticEffects().getFirst();
        assertThat(grant.scope()).isEqualTo(GrantScope.OWN_PERMANENTS);
        assertThat(grant.filter()).isEqualTo(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN));
        assertThat(grant.ability().getDescription()).contains("1 damage");
        assertThat(grant.ability().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Emblem persists after Koth dies")
    void emblemPersistsAfterKothDies() {
        Permanent koth = addReadyKoth(player1);
        koth.setLoyaltyCounters(5);

        // Use -5, Koth goes to 0 and dies
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Koth should be gone
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Koth of the Hammer"));
        // But emblem persists
        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player1.getId());
    }

    // ===== Loyalty ability restrictions =====

    @Test
    @DisplayName("Cannot activate -5 with only 3 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyKoth(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Koth dies when -2 brings loyalty to 1 (3 - 2 = 1, survives)")
    void minusTwoFromThreeLoyaltySurvives() {
        Permanent koth = addReadyKoth(player1);
        addMountain(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        GameData gd = harness.getGameData();
        // 3 - 2 = 1, Koth should survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Koth of the Hammer"));
        assertThat(koth.getLoyaltyCounters()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyKoth(Player player) {
        KothOfTheHammer card = new KothOfTheHammer();
        Permanent perm = new Permanent(card);
        perm.setLoyaltyCounters(3);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addMountain(Player player) {
        Mountain mountain = new Mountain();
        Permanent perm = new Permanent(mountain);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
