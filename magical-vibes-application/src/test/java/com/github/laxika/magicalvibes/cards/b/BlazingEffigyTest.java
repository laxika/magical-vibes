package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlazingEffigy.class, DoomBlade.class, GiantGrowth.class, PreyUpon.class, Shock.class,
        WallOfStone.class})
class BlazingEffigyTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger deals three damage to target creature")
    void deathTriggerDealsThreeDamage() {
        Permanent effigy = harness.addToBattlefieldAndReturn(player1, new BlazingEffigy());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfStone());
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castInstant(player2, 0, effigy.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Death trigger adds damage dealt by other Blazing Effigies and ignores other sources")
    void deathTriggerCountsDamageFromOtherNamedSources() {
        Permanent otherEffigy = harness.addToBattlefieldAndReturn(player1, new BlazingEffigy());
        Permanent dyingEffigy = harness.addToBattlefieldAndReturn(player2, new BlazingEffigy());
        Permanent wall = harness.addToBattlefieldAndReturn(player2, new WallOfStone());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, otherEffigy.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, dyingEffigy.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, List.of(otherEffigy.getId(), dyingEffigy.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, wall.getId());
        harness.passBothPriorities();

        assertThat(wall.getMarkedDamage()).isEqualTo(6);
    }
}
