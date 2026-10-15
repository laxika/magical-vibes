package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeveredStrands.class, GiantSpider.class, GrizzlyBears.class, Unsummon.class})
class SeveredStrandsTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the sacrificed creature's toughness and destroys an opponent's creature")
    void gainsLifeAndDestroysOpponentCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeveredStrands()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the spell's controller")
    void cannotTargetOwnCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeveredStrands()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeveredStrands()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's toughness including counters, not its printed toughness")
    void gainsLifeUsingModifiedToughness() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeveredStrands()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertNotOnBattlefield(player1, "Giant Spider");
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gains no life when the only target leaves before resolution, but the sacrifice remains paid")
    void doesNotGainLifeWhenTargetLeaves() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeveredStrands()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Giant Spider");
        harness.assertInGraveyard(player1, "Severed Strands");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
