package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BallistaSquad;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiligentZookeeper.class, AvenCloudchaser.class, BallistaSquad.class, AmoeboidChangeling.class})
class DiligentZookeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Gives non-Human creatures +1/+1 for each of their creature types")
    void boostsNonHumanCreaturesByTheirCreatureTypeCount() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        harness.addToBattlefield(player1, new AvenCloudchaser());
        harness.addToBattlefield(player1, new BallistaSquad());

        Permanent aven = findPermanent(player1, "Aven Cloudchaser");
        Permanent ballista = findPermanent(player1, "Ballista Squad");

        assertThat(gqs.getEffectivePower(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aven)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, ballista)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ballista)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost a Changeling because it is Human")
    void doesNotBoostChangelingBecauseItIsHuman() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        harness.addToBattlefield(player1, new AmoeboidChangeling());

        Permanent changeling = findPermanent(player1, "Amoeboid Changeling");

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not boost Human creatures or creatures controlled by an opponent")
    void excludesHumansAndOpponents() {
        harness.addToBattlefield(player1, new DiligentZookeeper());
        harness.addToBattlefield(player1, new BallistaSquad());
        harness.addToBattlefield(player2, new AvenCloudchaser());

        Permanent human = findPermanent(player1, "Ballista Squad");
        Permanent opponentAven = findPermanent(player2, "Aven Cloudchaser");

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentAven)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentAven)).isEqualTo(2);
    }
}
