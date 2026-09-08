package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OhranYetiTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana ability grants first strike to a snow creature")
    void grantsFirstStrikeToSnowCreature() {
        Permanent yeti = addReady(player1, new OhranYeti());
        Permanent snowCreature = addSnowCreature(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, yeti), 0, snowCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snowCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent yeti = addReady(player1, new OhranYeti());
        Permanent snowCreature = addSnowCreature(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, indexOf(player1, yeti), 0, snowCreature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, snowCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A nonsnow creature cannot be targeted")
    void rejectsNonsnowCreatureTarget() {
        Permanent yeti = addReady(player1, new OhranYeti());
        Permanent creature = addReady(player1, new GrizzlyBears());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, yeti), 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a snow creature");
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        Permanent yeti = addReady(player1, new OhranYeti());
        Permanent snowCreature = addSnowCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, yeti), 0, snowCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

    private Permanent addSnowCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        TestCards.mutableCard(creature).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
