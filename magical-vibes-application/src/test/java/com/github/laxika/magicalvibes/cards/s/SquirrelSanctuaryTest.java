package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SquirrelSanctuary.class, GrizzlyBears.class, Shock.class})
class SquirrelSanctuaryTest extends BaseCardTest {

    @Test
    void createsSquirrelWhenItEnters() {
        putSanctuaryOnBattlefield();

        assertThat(findPermanents(player1, "Squirrel")).hasSize(1);
    }

    @Test
    void mayPayToReturnItWhenAllyNontokenCreatureDies() {
        putSanctuaryOnBattlefield();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        killWithShock(bear);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Squirrel Sanctuary");
        harness.assertNotOnBattlefield(player1, "Squirrel Sanctuary");
    }

    @Test
    void decliningToPayKeepsItOnTheBattlefield() {
        putSanctuaryOnBattlefield();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(bear);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Squirrel Sanctuary");
        harness.assertNotInHand(player1, "Squirrel Sanctuary");
    }

    @Test
    void tokenCreatureDeathDoesNotTriggerReturnAbility() {
        putSanctuaryOnBattlefield();
        Permanent squirrel = findPermanents(player1, "Squirrel").getFirst();
        killWithShock(squirrel);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Squirrel Sanctuary");
    }

    private void putSanctuaryOnBattlefield() {
        harness.setHand(player1, List.of(new SquirrelSanctuary()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void killWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
