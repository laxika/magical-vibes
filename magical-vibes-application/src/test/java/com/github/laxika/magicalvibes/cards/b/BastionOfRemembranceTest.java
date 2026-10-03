package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BastionOfRemembrance.class, GrizzlyBears.class, Shock.class, Naturalize.class, Opalescence.class})
class BastionOfRemembranceTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield creates a 1/1 white Human Soldier token")
    void enteringBattlefieldCreatesHumanSoldier() {
        harness.setHand(player1, List.of(new BastionOfRemembrance()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Human Soldier");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature you control dying makes each opponent lose 1 life and gains you 1 life")
    void ownCreatureDeathDrainsOpponent() {
        harness.addToBattlefield(player1, new BastionOfRemembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger Bastion of Remembrance")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new BastionOfRemembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        killWithShock(player1, creature);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void createdTokenDeathDrainsOpponent() {
        harness.setHand(player1, List.of(new BastionOfRemembrance()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        killWithShock(player1, findPermanent(player1, "Human Soldier"));
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Human Soldier");
    }

    @Test
    void eachCreatureDeathInSameTurnTriggersSeparately() {
        harness.addToBattlefield(player1, new BastionOfRemembrance());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, first);
        harness.passBothPriorities();
        killWithShock(player1, second);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void eachBastionTriggersForOneCreatureDeath() {
        harness.addToBattlefield(player1, new BastionOfRemembrance());
        harness.addToBattlefield(player1, new BastionOfRemembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(player1, creature);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void deathTriggerResolvesAfterBastionIsDestroyed() {
        Permanent bastion = harness.addToBattlefieldAndReturn(player1, new BastionOfRemembrance());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(player1, creature);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, bastion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bastion of Remembrance");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void animatedBastionTriggersForItsOwnDeath() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent bastion = harness.addToBattlefieldAndReturn(player1, new BastionOfRemembrance());
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, bastion.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bastion of Remembrance");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, creature.getId());
    }
}
