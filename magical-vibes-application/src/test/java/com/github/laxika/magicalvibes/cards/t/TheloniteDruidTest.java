package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TheloniteDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature animates your Forests and leaves them as lands")
    void sacrificingCreatureAnimatesYourForests() {
        Permanent druid = addReadyDruid(player1);
        Permanent fodder = addReadyCreature(player1);
        Permanent forest = addLand(player1, new Forest());
        Permanent otherForest = addLand(player1, new Forest());
        Permanent mountain = addLand(player1, new Mountain());
        Permanent opponentForest = addLand(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(druid, forest, otherForest, mountain);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, otherForest)).isTrue();
        assertThat(gqs.isCreature(gd, mountain)).isFalse();
        assertThat(gqs.isCreature(gd, opponentForest)).isFalse();
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();
        assertThat(otherForest.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    @DisplayName("Forest animation wears off at end of turn")
    void forestAnimationWearsOffAtEndOfTurn() {
        addReadyDruid(player1);
        Permanent fodder = addReadyCreature(player1);
        Permanent forest = addLand(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        forest.resetModifiers();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("The Druid can be sacrificed to its own ability")
    void canSacrificeDruidItself() {
        addReadyDruid(player1);
        Permanent forest = addLand(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Thelonite Druid");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    private Permanent addReadyDruid(Player player) {
        Permanent permanent = new Permanent(new TheloniteDruid());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addLand(Player player, com.github.laxika.magicalvibes.model.Card land) {
        Permanent permanent = new Permanent(land);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
