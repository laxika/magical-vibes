package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ChasmDrake;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantasmalDragon.class, Shock.class, GiantGrowth.class, ProdigalPyromancer.class,
        Cancel.class, ChasmDrake.class})
class PhantasmalDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Phantasmal Dragon is sacrificed when targeted by an opponent's spell")
    void sacrificedWhenTargetedByOpponentSpell() {
        Permanent dragon = addCreatureReady(player1, new PhantasmalDragon());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, dragon.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Phantasmal Dragon is sacrificed when targeted by its controller's own spell")
    void sacrificedWhenTargetedByOwnSpell() {
        Permanent dragon = addCreatureReady(player1, new PhantasmalDragon());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, dragon.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Phantasmal Dragon is sacrificed when targeted by an activated ability")
    void sacrificedWhenTargetedByAbility() {
        Permanent dragon = addCreatureReady(player1, new PhantasmalDragon());

        Permanent pyro = addCreatureReady(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyro),
                null, dragon.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Phantasmal Dragon stays on the battlefield while untargeted")
    void survivesUntargetedRemoval() {
        addCreatureReady(player1, new PhantasmalDragon());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("A spell targeting a player does not trigger the Dragon's sacrifice ability")
    void survivesSpellTargetingPlayer() {
        harness.addToBattlefield(player1, new PhantasmalDragon());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertNotInGraveyard(player1, "Phantasmal Dragon");
    }

    @Test
    @DisplayName("Countering the targeting spell does not stop the sacrifice trigger")
    void sacrificedEvenIfTargetingSpellIsCountered() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new PhantasmalDragon());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, dragon.getId());
        harness.assertOnBattlefield(player1, "Phantasmal Dragon");
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Phantasmal Dragon");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A triggered ability targeting the Dragon triggers sacrifice before it resolves")
    void sacrificedWhenTargetedByTriggeredAbility() {
        addCreatureReady(player1, new ChasmDrake());
        Permanent dragon = addCreatureReady(player1, new PhantasmalDragon());

        declareAttackers(player1, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handlePermanentChosen(player1, dragon.getId()));
        harness.assertOnBattlefield(player1, "Phantasmal Dragon");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Dragon");
        harness.assertInGraveyard(player1, "Phantasmal Dragon");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Chasm Drake");
    }
}
