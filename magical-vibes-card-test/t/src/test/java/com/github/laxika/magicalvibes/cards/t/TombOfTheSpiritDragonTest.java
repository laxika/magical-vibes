package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.cards.w.WoollyLoxodon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TombOfTheSpiritDragon.class, GrizzlyBears.class, Juggernaut.class,
        SmiteTheMonstrous.class, WoollyLoxodon.class})
class TombOfTheSpiritDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TombOfTheSpiritDragon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains one life for each colorless creature controlled")
    void gainsLifeForControlledColorlessCreatures() {
        harness.addToBattlefield(player1, new TombOfTheSpiritDragon());
        harness.addToBattlefield(player1, new Juggernaut());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Juggernaut());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        harness.assertLife(player1, 11);
    }

    @Test
    void gainsNoLifeWithoutControlledColorlessCreatures() {
        harness.addToBattlefield(player1, new TombOfTheSpiritDragon());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Juggernaut());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player1, 10);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }

    @Test
    void gainsLifeForEachOfMultipleColorlessCreatures() {
        harness.addToBattlefield(player1, new TombOfTheSpiritDragon());
        harness.addToBattlefield(player1, new Juggernaut());
        harness.addToBattlefield(player1, new Juggernaut());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
    }

    @Test
    void countsCreaturesAtResolutionAfterOneIsDestroyed() {
        harness.addToBattlefield(player1, new TombOfTheSpiritDragon());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Juggernaut());
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new SmiteTheMonstrous()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player1, "Juggernaut");
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void countsFaceDownCreatureAsColorless() {
        harness.addToBattlefield(player1, new TombOfTheSpiritDragon());
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
    }

    @Test
    void excludesMorphTurnedFaceUpBeforeResolution() {
        harness.addToBattlefield(player1, new TombOfTheSpiritDragon());
        harness.setHand(player1, List.of(new WoollyLoxodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.turnFaceUp(player1, 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }
}
