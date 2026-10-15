package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdventOfTheWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnBurn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmmaraTandris.class, GrizzlyBears.class, Shock.class, AdventOfTheWurm.class, TurnBurn.class})
class EmmaraTandrisTest extends BaseCardTest {

    private Permanent addTokenCreature(UUID controllerId) {
        GrizzlyBears card = new GrizzlyBears();
        card.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(
                controllerId.equals(player1.getId()) ? player1 : player2, card);
        token.setSummoningSick(false);
        return token;
    }

    @Test
    @DisplayName("Noncombat damage to a creature token you control is prevented")
    void preventsNoncombatDamageToYourToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent token = addTokenCreature(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, token.getId());
        harness.passBothPriorities();

        assertThat(token.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Damage to a nontoken creature you control is not prevented")
    void doesNotPreventDamageToNontokenCreature() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Damage to an opponent's creature token is not prevented")
    void doesNotPreventDamageToOpponentToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent enemyToken = addTokenCreature(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, enemyToken.getId());
        harness.passBothPriorities();

        assertThat(enemyToken.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage to a creature token you control is prevented")
    void preventsCombatDamageToYourToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent blocker = addTokenCreature(player1.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Emmara protects a Wurm token created after she enters the battlefield")
    void protectsNewlyCreatedToken() {
        harness.addToBattlefield(player1, new EmmaraTandris());
        Permanent token = createWurmToken();

        harness.setHand(player2, List.of(new TurnBurn()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, token.getId());
        harness.passBothPriorities();

        assertThat(token.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    @DisplayName("Tokens are not protected after Turn removes Emmara's abilities")
    void losesProtectionWhenEmmaraLosesAbilities() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraTandris());
        Permanent token = createWurmToken();

        harness.setHand(player2, List.of(new TurnBurn()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player2, 0, 2, List.of(emmara.getId(), token.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(emmara, token);
        assertThat(token.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tokens are not protected after Emmara leaves the battlefield")
    void losesProtectionWhenEmmaraLeavesBattlefield() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraTandris());
        Permanent token = createWurmToken();

        harness.setHand(player2, List.of(new TurnBurn(), new TurnBurn()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castModalInstant(player2, 0, 2, List.of(emmara.getId(), emmara.getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Emmara Tandris");
        harness.assertInGraveyard(player1, "Emmara Tandris");

        harness.castInstant(player2, 0, 1, token.getId());
        harness.passBothPriorities();

        assertThat(token.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    @DisplayName("Emmara does not prevent damage to herself")
    void doesNotProtectHerself() {
        Permanent emmara = harness.addToBattlefieldAndReturn(player1, new EmmaraTandris());

        harness.setHand(player2, List.of(new TurnBurn()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 1, emmara.getId());
        harness.passBothPriorities();

        assertThat(emmara.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(emmara);
    }

    private Permanent createWurmToken() {
        harness.setHand(player1, List.of(new AdventOfTheWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
    }
}
