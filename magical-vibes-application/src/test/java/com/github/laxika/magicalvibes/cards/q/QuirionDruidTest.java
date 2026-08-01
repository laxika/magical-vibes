package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuirionDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes a 2/2 green creature that's still a land")
    void targetLandBecomesTwoTwoGreenCreature() {
        addDruid(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, land)).contains(CardColor.GREEN);
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Animation survives end-of-turn cleanup")
    void animationSurvivesCleanup() {
        addDruid(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        land.resetModifiers();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, land)).contains(CardColor.GREEN);
    }

    @Test
    @DisplayName("Can animate an opponent's land")
    void canAnimateOpponentLand() {
        addDruid(player1);
        Permanent opponentLand = addLand(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentLand.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gqs.isCreature(gd, opponentLand)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentLand)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonLand() {
        addDruid(player1);
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(creature);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target land leaves before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addDruid(player1);
        Permanent land = addLand(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    private Permanent addDruid(Player player) {
        Permanent perm = new Permanent(new QuirionDruid());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addLand(Player player) {
        Permanent perm = new Permanent(new Forest());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
