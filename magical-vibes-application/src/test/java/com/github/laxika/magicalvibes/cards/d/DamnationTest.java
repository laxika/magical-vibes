package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.cards.s.SealOfPrimordium;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Damnation.class, GossamerPhantasm.class, HedgeTroll.class, SealOfPrimordium.class})
class DamnationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and leaves noncreature permanents alone")
    void destroysAllCreatures() {
        harness.addToBattlefield(player1, new GossamerPhantasm());
        harness.addToBattlefield(player2, new HedgeTroll());
        harness.addToBattlefield(player1, new SealOfPrimordium());
        castDamnation();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertNotOnBattlefield(player2, "Hedge Troll");
        harness.assertOnBattlefield(player1, "Seal of Primordium");
    }

    @Test
    @DisplayName("Creatures cannot regenerate from Damnation")
    void creaturesCannotRegenerate() {
        Permanent troll = addCreatureReady(player2, new HedgeTroll());
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);

        castDamnation();

        harness.assertNotOnBattlefield(player2, "Hedge Troll");
        harness.assertInGraveyard(player2, "Hedge Troll");
    }

    private void castDamnation() {
        harness.castFromHand(player1, new Damnation(), "{2}{B}{B}");
        harness.passBothPriorities();
    }
}
