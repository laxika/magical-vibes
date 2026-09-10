package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinWelder;
import com.github.laxika.magicalvibes.cards.w.WeatherseedElf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EngineeredPlague.class, GoblinWelder.class, WeatherseedElf.class})
class EngineeredPlagueTest extends BaseCardTest {

    private Permanent addPlague(CardSubtype chosen) {
        Permanent plague = harness.addToBattlefieldAndReturn(player1, new EngineeredPlague());
        plague.setChosenSubtype(chosen);
        return plague;
    }

    @Test
    @DisplayName("Choosing a creature type on enter, then all creatures of that type get -1/-1")
    void choosesTypeOnEnter() {
        harness.setHand(player1, List.of(new EngineeredPlague()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();          // resolve -> subtype choice pends
        harness.handleListChoice(player1, "GOBLIN");

        Permanent goblinPerm = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Own creatures of the chosen type get -1/-1")
    void weakensOwnCreaturesOfChosenType() {
        Permanent goblinPerm = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type also get -1/-1")
    void weakensOpponentCreaturesOfChosenType() {
        Permanent goblinPerm = harness.addToBattlefieldAndReturn(player2, new GoblinWelder());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Creatures of a different type are not affected")
    void doesNotAffectOtherTypes() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player1, new WeatherseedElf());
        addPlague(CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("The -1/-1 disappears when Engineered Plague leaves the battlefield")
    void effectRemovedWhenPlagueLeaves() {
        Permanent goblinPerm = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        Permanent plague = addPlague(CardSubtype.GOBLIN);

        assertThat(gqs.computeStaticBonus(gd, goblinPerm).power()).isEqualTo(-1);

        gd.playerBattlefields.get(player1.getId()).remove(plague);
        assertThat(gqs.computeStaticBonus(gd, goblinPerm).power()).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature with the chosen type among multiple types is affected")
    void matchesAnyCreatureSubtype() {
        Permanent goblinPerm = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        addPlague(CardSubtype.ARTIFICER);

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(-1);
        assertThat(bonus.toughness()).isEqualTo(-1);
    }
}
