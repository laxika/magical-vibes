package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpendingDisasterTest extends BaseCardTest {

    @Test
    @DisplayName("Seven lands on the battlefield sacrifice Impending Disaster and destroy all lands")
    void sevenLandsSacrificeAndDestroyAllLands() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(disaster);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Impending Disaster");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fewer than seven lands do not trigger Impending Disaster")
    void fewerThanSevenLandsDoNotTrigger() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(disaster);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The upkeep trigger rechecks the land count when it resolves")
    void rechecksLandCountAtResolution() {
        Permanent disaster = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        Permanent removed = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.addToBattlefield(player2, new Island());

        advanceToUpkeep(player1);
        gd.playerBattlefields.get(player2.getId()).remove(removed);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(disaster);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().hasType(CardType.LAND));
    }
}
