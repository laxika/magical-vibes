package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DaybreakRanger;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegenerateAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class FullMoonsRiseTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Full Moon's Rise has static boost effect for Werewolves with trample")
    void hasCorrectStaticEffect() {
        FullMoonsRise card = new FullMoonsRise();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isZero();
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.TRAMPLE);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    @Test
    @DisplayName("Full Moon's Rise has sacrifice activated ability with regenerate all effect")
    void hasCorrectActivatedAbility() {
        FullMoonsRise card = new FullMoonsRise();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(RegenerateAllOwnCreaturesEffect.class);
    }

    // ===== Static effect: buffs Werewolves you control =====

    @Test
    @DisplayName("Werewolf creatures you control get +1/+0 and trample")
    void buffsWerewolvesYouControl() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());

        Permanent ranger = findPermanent(player1, "Daybreak Ranger");

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2); // 2 base + 0 static
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not buff non-Werewolf creatures")
    void doesNotBuffNonWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Werewolf creatures")
    void doesNotBuffOpponentWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player2, new DaybreakRanger());

        Permanent opponentRanger = findPermanent(player2, "Daybreak Ranger");

        assertThat(gqs.getEffectivePower(gd, opponentRanger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentRanger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentRanger, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Bonus is removed when Full Moon's Rise leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());

        Permanent ranger = findPermanent(player1, "Daybreak Ranger");
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Full Moon's Rise"));

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Sacrifice ability: regenerate all Werewolves =====

    @Test
    @DisplayName("Activating sacrifice ability puts it on the stack and sacrifices Full Moon's Rise")
    void activationSacrificesAndStacksAbility() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Full Moon's Rise"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Full Moon's Rise"));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Full Moon's Rise");
    }

    @Test
    @DisplayName("Resolving sacrifice ability gives regeneration shields to all Werewolves you control")
    void regeneratesAllWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player1, new DaybreakRanger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Both werewolves should have a regeneration shield
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Daybreak Ranger"))
                .forEach(p -> assertThat(p.getRegenerationShield()).isEqualTo(1));
    }

    @Test
    @DisplayName("Regeneration does not affect non-Werewolf creatures")
    void regenerationDoesNotAffectNonWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getRegenerationShield()).isZero();

        Permanent ranger = findPermanent(player1, "Daybreak Ranger");
        assertThat(ranger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration does not affect opponent's Werewolf creatures")
    void regenerationDoesNotAffectOpponentWerewolves() {
        harness.addToBattlefield(player1, new FullMoonsRise());
        harness.addToBattlefield(player1, new DaybreakRanger());
        harness.addToBattlefield(player2, new DaybreakRanger());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ownRanger = findPermanent(player1, "Daybreak Ranger");
        assertThat(ownRanger.getRegenerationShield()).isEqualTo(1);

        Permanent opponentRanger = findPermanent(player2, "Daybreak Ranger");
        assertThat(opponentRanger.getRegenerationShield()).isZero();
    }

    // ===== Helper methods =====

    private Permanent findPermanent(Player player, String cardName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(cardName))
                .findFirst().orElseThrow();
    }
}
