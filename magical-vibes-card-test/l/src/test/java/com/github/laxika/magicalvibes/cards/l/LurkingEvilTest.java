package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingEvil.class, Clone.class, Disenchant.class})
class LurkingEvilTest extends BaseCardTest {

    @Test
    @DisplayName("Paying half your life makes Lurking Evil a 4/4 Phyrexian Horror with flying")
    void becomesCreatureAfterPayingHalfLife() {
        harness.setLife(player1, 20);
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, evil)).isTrue();
        assertThat(gqs.isEnchantment(gd, evil)).isFalse();
        assertThat(gqs.getEffectivePower(gd, evil)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, evil)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, evil))
                .containsExactlyInAnyOrder(CardSubtype.PHYREXIAN, CardSubtype.HORROR);
        assertThat(gqs.hasKeyword(gd, evil, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Half your life rounds up for an odd life total")
    void halfLifeRoundsUp() {
        harness.setLife(player1, 21);
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, evil)).isTrue();
    }

    @Test
    @DisplayName("Lurking Evil remains a creature after end-of-turn cleanup")
    void animationIsPermanent() {
        harness.setLife(player1, 20);
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        gd.expireEndOfTurnFloatingEffects();
        evil.resetModifiers();

        assertThat(gqs.isCreature(gd, evil)).isTrue();
        assertThat(gqs.isEnchantment(gd, evil)).isFalse();
    }

    @Test
    @DisplayName("Animation occurs on resolution, not when life is paid")
    void remainsEnchantmentWhileAbilityIsOnStack() {
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 10);
        assertThat(gqs.isCreature(gd, evil)).isFalse();
        assertThat(gqs.isEnchantment(gd, evil)).isTrue();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, evil.getId());
        harness.assertInGraveyard(player1, "Lurking Evil");

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Lurking Evil");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("An animated Lurking Evil retains its activated ability")
    void canActivateAgainAfterBecomingCreature() {
        harness.setLife(player1, 21);
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.assertLife(player1, 5);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, evil)).isTrue();
        assertThat(gqs.getEffectivePower(gd, evil)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, evil)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, evil, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Copying animated Lurking Evil does not copy its animation")
    void cloneEntersAsUnanimatedEnchantment() {
        Permanent evil = harness.addToBattlefieldAndReturn(player1, new LurkingEvil());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, evil.getId());

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(evil.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isEnchantment(gd, copy)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 1, null, null);
        harness.assertLife(player1, 5);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, copy)).isTrue();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isTrue();
    }
}
