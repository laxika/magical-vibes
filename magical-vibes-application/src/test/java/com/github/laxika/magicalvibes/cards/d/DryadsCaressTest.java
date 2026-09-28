package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.OvergrownTomb;
import com.github.laxika.magicalvibes.cards.z.ZephyrSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DryadsCaress.class, ZephyrSpirit.class, OvergrownTomb.class})
class DryadsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each creature on the battlefield")
    void gainsLifeForEachCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());
        ownCreature.tap();
        ownOtherCreature.tap();
        opposingCreature.tap();

        harness.setLife(player1, 20);
        harness.castFromHand(player1, new DryadsCaress(), "{4}{G}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(ownOtherCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps your creatures when white mana was spent")
    void untapsOwnCreaturesWhenWhiteWasSpent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ZephyrSpirit());
        ownCreature.tap();
        opposingCreature.tap();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DryadsCaress()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 22);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counts and untaps creatures but not noncreature permanents")
    void onlyCountsAndUntapsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ZephyrSpirit());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new OvergrownTomb());
        ownCreature.tap();
        ownLand.tap();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DryadsCaress()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 21);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
    }
}
