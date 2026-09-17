package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinBrigand;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.w.WirewoodSymbiote;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientOoze.class, ArkOfBlight.class, GoblinBrigand.class,
        ScornfulEgotist.class, WirewoodSymbiote.class})
class AncientOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Counts the total mana value of other creatures you control")
    void countsOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new GoblinBrigand());
        harness.addToBattlefield(player1, new WirewoodSymbiote());
        harness.addToBattlefield(player2, new GoblinBrigand());
        harness.addToBattlefield(player1, new ArkOfBlight());
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new AncientOoze());

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates as your other creatures enter and leave")
    void updatesDynamically() {
        Permanent brigand = harness.addToBattlefieldAndReturn(player1, new GoblinBrigand());
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new AncientOoze());

        assertStats(ooze, 2, 2);

        Permanent symbiote = harness.addToBattlefieldAndReturn(player1, new WirewoodSymbiote());
        assertStats(ooze, 3, 3);

        gd.playerBattlefields.get(player1.getId()).remove(symbiote);
        assertStats(ooze, 2, 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brigand, ooze);
    }

    @Test
    @DisplayName("Treats a face-down creature as having mana value zero")
    void faceDownCreatureContributesZeroManaValue() {
        Permanent faceDown = harness.addToBattlefieldAndReturn(player1, new ScornfulEgotist());
        faceDown.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new AncientOoze());

        assertStats(ooze, 0, 0);
    }

    private void assertStats(Permanent ooze, int power, int toughness) {
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(toughness);
    }
}
