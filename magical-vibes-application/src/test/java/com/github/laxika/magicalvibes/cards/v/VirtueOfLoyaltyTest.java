package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArdenvaleFealty;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirtueOfLoyalty.class, ArdenvaleFealty.class, GrizzlyBears.class})
class VirtueOfLoyaltyTest extends BaseCardTest {

    @Test
    void adventureCreatesVigilantKnightAndExilesCard() {
        VirtueOfLoyalty card = new VirtueOfLoyalty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Knight");
        assertThat(knight.getCard().isToken()).isTrue();
        assertThat(knight.getCard().getKeywords()).contains(Keyword.VIGILANCE);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void enchantmentFaceCanBeCastFromExileAfterAdventure() {
        VirtueOfLoyalty card = new VirtueOfLoyalty();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Virtue of Loyalty");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }

    @Test
    void endStepPutsCountersOnAndUntapsOnlyControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        opponentCreature.tap();
        harness.addToBattlefield(player1, new VirtueOfLoyalty());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.CLEANUP));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.CLEANUP));
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCreature.isTapped()).isTrue();
    }
}
