package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatGoblinFoulHearted.class, GrizzlyBears.class})
class GreatGoblinFoulHeartedTest extends BaseCardTest {

    @Test
    @DisplayName("ETB amasses Goblins 3 and gives the Army trample")
    void entersAndAmassesGoblins() {
        castGreatGoblin();

        Permanent army = findPermanent(player1, "Goblin Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getCard().getSubtypes())
                .containsExactly(CardSubtype.GOBLIN, CardSubtype.ARMY);
        assertThat(gqs.hasKeyword(gd, army, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Amass puts three counters on an existing Army")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castGreatGoblin();

        assertThat(findPermanents(player1, "Goblin Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(gqs.hasKeyword(gd, army, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Attacking amasses Goblins 3")
    void attacksAndAmassesGoblins() {
        addCreatureReady(player1, new GreatGoblinFoulHearted());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Goblin Army")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only Armies controlled by Great Goblin's controller gain trample")
    void onlyOwnArmiesGainTrample() {
        Permanent ownArmy = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        addCreatureReady(player1, new GreatGoblinFoulHearted());

        assertThat(gqs.hasKeyword(gd, ownArmy, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingArmy, Keyword.TRAMPLE)).isFalse();
    }

    private void castGreatGoblin() {
        harness.setHand(player1, List.of(new GreatGoblinFoulHearted()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
