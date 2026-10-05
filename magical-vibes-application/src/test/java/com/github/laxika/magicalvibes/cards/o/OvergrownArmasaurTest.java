package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RecklessRage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OvergrownArmasaur.class, Shock.class, GrizzlyBears.class, RecklessRage.class})
class OvergrownArmasaurTest extends BaseCardTest {

    @Test
    void spellDamageCreatesSaprolingToken() {
        harness.addToBattlefield(player2, new OvergrownArmasaur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID armasaurId = harness.getPermanentId(player2, "Overgrown Armasaur");
        harness.castInstant(player1, 0, armasaurId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Saproling")
                        && permanent.getCard().getPower() == 1
                        && permanent.getCard().getToughness() == 1);
    }

    @Test
    void combatDamageCreatesSaprolingToken() {
        harness.addToBattlefield(player2, new OvergrownArmasaur());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = findPermanent(player1, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent armasaur = findPermanent(player2, "Overgrown Armasaur");
        armasaur.setSummoningSick(false);
        armasaur.setBlocking(true);
        armasaur.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Saproling"));
    }

    @Test
    void lethalDamageStillCreatesOneTokenForEachDamagedArmasaurController() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new OvergrownArmasaur());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new OvergrownArmasaur());
        harness.setHand(player1, List.of(new RecklessRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(opponent.getId(), own.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Overgrown Armasaur");
        harness.assertOnBattlefield(player1, "Overgrown Armasaur");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getCard().isToken());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling")))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling")))
                .hasSize(1);
    }

    @Test
    void separateDamageEventsEachCreateATokenEvenWhenTheSecondIsLethal() {
        Permanent armasaur = harness.addToBattlefieldAndReturn(player1, new OvergrownArmasaur());
        Permanent firstOpponent = harness.addToBattlefieldAndReturn(player2, new OvergrownArmasaur());
        Permanent secondOpponent = harness.addToBattlefieldAndReturn(player2, new OvergrownArmasaur());
        harness.setHand(player1, List.of(new RecklessRage(), new RecklessRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, List.of(firstOpponent.getId(), armasaur.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castInstant(player1, 0, List.of(secondOpponent.getId(), armasaur.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Overgrown Armasaur");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling")))
                .hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Saproling")))
                .hasSize(2);
    }
}
