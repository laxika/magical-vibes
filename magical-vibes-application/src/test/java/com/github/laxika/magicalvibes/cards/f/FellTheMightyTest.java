package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FellTheMighty.class, AirElemental.class, GrizzlyBears.class, HillGiant.class, HowlingMine.class, Forest.class})
class FellTheMightyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys creatures with power greater than the target's power")
    void destroysCreaturesWithGreaterPower() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new AirElemental());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var target = harness.getPermanentId(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new FellTheMighty()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        var target = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
