package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkoThiefOfCrowns.class, FountainOfYouth.class, GrizzlyBears.class})
class OkoThiefOfCrownsTest extends BaseCardTest {

    @Test
    void createsFoodToken() {
        Permanent oko = addReadyOko(player1, 3);

        harness.activateAbility(player1, battlefieldIndex(player1, oko), 0, null, null);
        harness.passBothPriorities();

        Permanent food = findPermanent(player1, "Food");
        assertThat(gqs.isArtifact(gd, food)).isTrue();
        assertThat(food.getCard().getSubtypes()).contains(CardSubtype.FOOD);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void transformsArtifactIntoGreenElk() {
        Permanent oko = addReadyOko(player1, 3);
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.activateAbility(player1, battlefieldIndex(player1, oko), 1, null, fountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.isArtifact(gd, fountain)).isFalse();
        assertThat(gqs.isCreature(gd, fountain)).isTrue();
        assertThat(gqs.hasColor(gd, fountain, CardColor.GREEN)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, fountain)).containsExactly(CardSubtype.ELK);
        assertThat(gqs.getEffectivePower(gd, fountain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isEqualTo(3);
        assertThat(gqs.hasLostAllAbilities(gd, fountain)).isTrue();
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void exchangesControlledArtifactForSmallOpponentCreature() {
        Permanent oko = addReadyOko(player1, 5);
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(player1, oko), 2,
                List.of(fountain.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(fountain);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
    }

    private Permanent addReadyOko(Player player, int loyalty) {
        Permanent oko = new Permanent(new OkoThiefOfCrowns());
        oko.setCounterCount(CounterType.LOYALTY, loyalty);
        oko.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(oko);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return oko;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
