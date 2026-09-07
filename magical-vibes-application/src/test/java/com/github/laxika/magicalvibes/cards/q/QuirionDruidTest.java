package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.Chaoslace;
import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuirionDruid.class, Quicksand.class, ManOWar.class, Chaoslace.class})
class QuirionDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes a 2/2 green creature that's still a land")
    void targetLandBecomesTwoTwoGreenCreature() {
        Permanent druid = addCreatureReady(player1, new QuirionDruid());
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, land)).contains(CardColor.GREEN);
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Animation survives end-of-turn cleanup")
    void animationSurvivesCleanup() {
        addCreatureReady(player1, new QuirionDruid());
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        gd.expireEndOfTurnFloatingEffects();
        land.resetModifiers();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, land)).contains(CardColor.GREEN);
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Can animate an opponent's land")
    void canAnimateOpponentLand() {
        addCreatureReady(player1, new QuirionDruid());
        Permanent opponentLand = addLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentLand.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opponentLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentLand)).isEqualTo(2);
        assertThat(gqs.isLand(gd, opponentLand)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, opponentLand)).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new QuirionDruid());
        Permanent creature = addCreatureReady(player1, new ManOWar());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target land leaves before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addCreatureReady(player1, new QuirionDruid());
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    @DisplayName("Animation's green color replaces an earlier color setting")
    void animationReplacesEarlierColorSetting() {
        addCreatureReady(player1, new QuirionDruid());
        Permanent land = addLand(player1);

        harness.setHand(player1, List.of(new Chaoslace()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, land.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, land)).containsExactly(CardColor.GREEN);
    }

    private Permanent addLand(Player player) {
        Permanent perm = new Permanent(new Quicksand());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
