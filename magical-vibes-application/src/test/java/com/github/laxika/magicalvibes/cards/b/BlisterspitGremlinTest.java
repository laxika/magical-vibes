package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlisterspitGremlin.class, Shock.class, GrizzlyBears.class})
class BlisterspitGremlinTest extends BaseCardTest {

    @Test
    @DisplayName("Paying mana and tapping Blisterspit Gremlin deals 1 damage to each opponent")
    void activatedAbilityDamagesOpponent() {
        Permanent gremlin = addReadyGremlin();
        int startingLife = gd.getLife(player2.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gremlin.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Casting a noncreature spell untaps Blisterspit Gremlin")
    void noncreatureSpellUntapsGremlin() {
        Permanent gremlin = addReadyGremlin();
        gremlin.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gremlin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a creature spell does not untap Blisterspit Gremlin")
    void creatureSpellDoesNotUntapGremlin() {
        Permanent gremlin = addReadyGremlin();
        gremlin.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gremlin.isTapped()).isTrue();
    }

    private Permanent addReadyGremlin() {
        Permanent gremlin = new Permanent(new BlisterspitGremlin());
        gremlin.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gremlin);
        return gremlin;
    }
}
