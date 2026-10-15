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
        Permanent gremlin = addCreatureReady(player1, new BlisterspitGremlin());
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
        Permanent gremlin = addCreatureReady(player1, new BlisterspitGremlin());
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
        Permanent gremlin = addCreatureReady(player1, new BlisterspitGremlin());
        gremlin.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gremlin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untap trigger resolves before the noncreature spell")
    void untapResolvesBeforeSpell() {
        Permanent gremlin = addCreatureReady(player1, new BlisterspitGremlin());
        gremlin.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int startingLife = gd.getLife(player2.getId());

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gremlin.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gremlin.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 2);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not untap the Gremlin")
    void opponentsSpellDoesNotUntapGremlin() {
        Permanent gremlin = addCreatureReady(player2, new BlisterspitGremlin());
        gremlin.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gremlin.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The Gremlin can activate again after its cast trigger untaps it")
    void canActivateAgainAfterUntapping() {
        Permanent gremlin = addCreatureReady(player1, new BlisterspitGremlin());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gremlin.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gremlin.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 4);
    }
}
