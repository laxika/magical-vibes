package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheDarknessCrystal.class, DoomBlade.class, GrizzlyBears.class, Shock.class})
class TheDarknessCrystalTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the generic cost of black spells you cast")
    void reducesBlackSpellCost() {
        harness.addToBattlefield(player1, new TheDarknessCrystal());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getId())
                .contains(target.getCard().getId());
    }

    @Test
    @DisplayName("Exiles an opponent's nontoken creature and gains two life when it would die")
    void exilesOpponentCreatureAndGainsLife() {
        harness.addToBattlefield(player1, new TheDarknessCrystal());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .doesNotContain(target.getCard().getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Returns a targeted exiled creature tapped with two additional +1/+1 counters")
    void returnsTargetedCreatureWithCounters() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new TheDarknessCrystal());
        GrizzlyBears bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, crystal.getId());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, bears.getId(), Zone.EXILE);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }
}
