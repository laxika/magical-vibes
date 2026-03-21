package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TapSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DrudgeSentinelTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with TapSelfEffect and GrantKeywordEffect(INDESTRUCTIBLE, SELF)")
    void hasActivatedAbility() {
        DrudgeSentinel card = new DrudgeSentinel();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{3}");
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(TapSelfEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) ability.getEffects().get(1);
        assertThat(grant.keywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(grant.scope()).isEqualTo(GrantScope.SELF);
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability taps and grants indestructible")
    void activatingAbilityTapsAndGrantsIndestructible() {
        Permanent sentinel = addReadySentinel(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Can activate ability when already tapped — still gains indestructible")
    void canActivateWhenAlreadyTapped() {
        Permanent sentinel = addReadySentinel(player1);
        sentinel.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Casting from hand, then activating ability works")
    void castThenActivate() {
        harness.setHand(player1, List.of(new DrudgeSentinel()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sentinel = findPermanent(player1, "Drudge Sentinel");
        assertThat(sentinel).isNotNull();

        // Remove summoning sickness and add mana for ability
        sentinel.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(sentinel);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(sentinel.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    // ===== Helpers =====

    private Permanent addReadySentinel(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = new Permanent(new DrudgeSentinel());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
