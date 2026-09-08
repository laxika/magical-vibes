package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HavenwoodBattleground;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheloniteDruid.class, Thallid.class, Forest.class, HavenwoodBattleground.class})
class TheloniteDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature animates your Forests and leaves them as lands")
    void sacrificingCreatureAnimatesYourForests() {
        Permanent druid = addCreatureReady(player1, new TheloniteDruid());
        Permanent fodder = addCreatureReady(player1, new Thallid());
        Permanent forest = addLand(player1, new Forest());
        Permanent otherForest = addLand(player1, new Forest());
        Permanent nonForestLand = addLand(player1, new HavenwoodBattleground());
        Permanent opponentForest = addLand(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(druid, forest, otherForest, nonForestLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodder);
        assertThat(druid.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, otherForest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherForest)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherForest)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, nonForestLand)).isFalse();
        assertThat(gqs.isCreature(gd, opponentForest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, otherForest)).isTrue();
    }

    @Test
    @DisplayName("Forest animation wears off at end of turn")
    void forestAnimationWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new TheloniteDruid());
        Permanent fodder = addCreatureReady(player1, new Thallid());
        Permanent forest = addLand(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("The Druid can be sacrificed to its own ability")
    void canSacrificeDruidItself() {
        addCreatureReady(player1, new TheloniteDruid());
        Permanent forest = addLand(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Thelonite Druid");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
    }

    private Permanent addLand(Player player, com.github.laxika.magicalvibes.model.Card land) {
        Permanent permanent = new Permanent(land);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
