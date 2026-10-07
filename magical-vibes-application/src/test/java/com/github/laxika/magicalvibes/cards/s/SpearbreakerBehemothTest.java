package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpearbreakerBehemoth.class, AvatarOfMight.class, GrizzlyBears.class})
class SpearbreakerBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants indestructible to a target creature with power 5 or greater")
    void grantsIndestructibleToBigCreature() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Granted creature survives lethal damage")
    void survivesLethalDamage() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        avatar.setMarkedDamage(20);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("Indestructible wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetSmallCreature() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 5 or greater");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Behemoth can grant indestructible to an opponent's creature")
    void canActivateWhileTappedAndSummoningSickForOpponent() {
        Permanent behemoth = harness.addToBattlefieldAndReturn(player1, new SpearbreakerBehemoth());
        behemoth.setSummoningSick(true);
        behemoth.setTapped(true);
        Permanent avatar = addReady(player2, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("A creature with exactly five effective power is a legal target")
    void canTargetExactlyFiveEffectivePower() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        avatar.setPowerModifier(-3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The ability does not resolve if the target's power drops below five")
    void targetBecomesIllegalBeforeResolution() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        avatar.setPowerModifier(-4);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible remains if the creature's power drops below five after resolution")
    void grantRemainsAfterPowerDrops() {
        addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();
        avatar.setPowerModifier(-4);
        avatar.setMarkedDamage(20);
        harness.runStateBasedActions();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertOnBattlefield(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("The ability still resolves when Spearbreaker Behemoth leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent behemoth = addReady(player1, new SpearbreakerBehemoth());
        Permanent avatar = addReady(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        gd.playerBattlefields.get(player1.getId()).remove(behemoth);
        gd.playerGraveyards.get(player1.getId()).add(behemoth.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, avatar, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Spearbreaker Behemoth survives lethal damage without activating its ability")
    void intrinsicIndestructiblePreventsLethalDamage() {
        Permanent behemoth = addReady(player1, new SpearbreakerBehemoth());
        behemoth.setMarkedDamage(20);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Spearbreaker Behemoth");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
