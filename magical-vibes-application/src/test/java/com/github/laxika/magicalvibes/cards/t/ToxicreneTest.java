package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DunesOfTheDead;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Toxicrene.class, HostileDesert.class, DunesOfTheDead.class, StoneRain.class})
class ToxicreneTest extends BaseCardTest {

    @Test
    void allLandsCanProduceAnyColor() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        resolveToxicrene();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void landsLoseTheirOtherAbilities() {
        harness.addToBattlefield(player2, new DunesOfTheDead());
        resolveToxicrene();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Dunes of the Dead"));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    private void resolveToxicrene() {
        harness.setHand(player1, List.of(new Toxicrene()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
