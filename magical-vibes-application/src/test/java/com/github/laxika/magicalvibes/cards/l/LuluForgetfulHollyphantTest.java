package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LuluForgetfulHollyphant.class, AirElemental.class, GrizzlyBears.class})
class LuluForgetfulHollyphantTest extends BaseCardTest {

    @Test
    void perpetuallyGivesFlyingToTheNextNonFlyingCreatureSpell() {
        LuluForgetfulHollyphant lulu = new LuluForgetfulHollyphant();
        GrizzlyBears bears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(lulu, bears, secondBears));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(bears.getId()))
                .findFirst()
                .orElseThrow();
        Permanent secondPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(secondBears.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondPermanent, Keyword.FLYING)).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }

    @Test
    void flyingCreatureDoesNotConsumeTheBoon() {
        LuluForgetfulHollyphant lulu = new LuluForgetfulHollyphant();
        AirElemental flyer = new AirElemental();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(lulu, flyer, bears));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent permanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(candidate -> candidate.getCard().getId().equals(bears.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
    }
}
