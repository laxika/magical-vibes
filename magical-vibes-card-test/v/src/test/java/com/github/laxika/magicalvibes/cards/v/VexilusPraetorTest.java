package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.cards.e.Exterminatus;
import com.github.laxika.magicalvibes.cards.i.InquisitorGreyfax;
import com.github.laxika.magicalvibes.cards.s.SicarianInfiltrator;
import com.github.laxika.magicalvibes.cards.s.Skullclamp;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VexilusPraetor.class, InquisitorGreyfax.class, SicarianInfiltrator.class,
        CommandTower.class, SwordsToPlowshares.class, BlasphemousAct.class, Skullclamp.class,
        Exterminatus.class})
class VexilusPraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Your commanders have protection from everything")
    void protectsYourCommandersFromEverySource() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        Permanent creatureSource = harness.addToBattlefieldAndReturn(player2, new SicarianInfiltrator());
        Permanent landSource = harness.addToBattlefieldAndReturn(player2, new CommandTower());

        assertThat(gqs.hasProtectionFromSource(gd, commander, creatureSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, commander, landSource)).isTrue();
    }

    @Test
    @DisplayName("Protects only commanders you control")
    void doesNotProtectNonCommandersOrOpponentsCommanders() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent ownCommander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), ownCommander.getCard());
        Permanent ownNonCommander = harness.addToBattlefieldAndReturn(player1, new SicarianInfiltrator());
        Permanent opponentCommander = harness.addToBattlefieldAndReturn(player2, new InquisitorGreyfax());
        gd.makeCommander(player2.getId(), opponentCommander.getCard());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new SicarianInfiltrator());

        assertThat(gqs.hasProtectionFromSource(gd, ownCommander, source)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, ownNonCommander, source)).isFalse();
        assertThat(gqs.hasProtectionFromSource(gd, opponentCommander, source)).isFalse();
    }

    @Test
    void protectionFollowsControlRatherThanCommanderOwnership() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player2, new InquisitorGreyfax());
        gd.makeCommander(player2.getId(), commander.getCard());
        Permanent source = harness.addToBattlefieldAndReturn(player2, new SicarianInfiltrator());

        gd.playerBattlefields.get(player2.getId()).remove(commander);
        gd.playerBattlefields.get(player1.getId()).add(commander);
        assertThat(gqs.hasProtectionFromSource(gd, commander, source)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(commander);
        gd.playerBattlefields.get(player2.getId()).add(commander);
        assertThat(gqs.hasProtectionFromSource(gd, commander, source)).isFalse();
    }

    @Test
    void evenYourOwnSpellCannotTargetYourCommander() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void commanderLosesProtectionWhenPraetorIsExiled() {
        Permanent praetor = harness.addToBattlefieldAndReturn(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, praetor.getId());

        harness.assertNotOnBattlefield(player1, "Vexilus Praetor");
        assertThat(gqs.hasProtectionFromSource(gd, commander, new SwordsToPlowshares())).isFalse();
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, commander.getId());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void preventsUntargetedDamageEvenWhenThatDamageKillsPraetor() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        harness.castFromHand(player1, new BlasphemousAct(), "{8}{R}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vexilus Praetor");
        harness.assertOnBattlefield(player1, "Inquisitor Greyfax");
        assertThat(commander.getMarkedDamage()).isZero();
        assertThat(gqs.hasProtectionFromSource(gd, commander, new BlasphemousAct())).isFalse();
    }

    @Test
    void protectedCommanderCannotBeBlocked() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = addCreatureReady(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        commander.setAttacking(true);
        addCreatureReady(player2, new SicarianInfiltrator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void protectionDetachesEquipmentAndPreventsReequipping() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Skullclamp());
        equipment.setAttachedTo(commander.getId());

        harness.runStateBasedActions();

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Skullclamp");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flashCanGrantProtectionInResponseToRemoval() {
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, commander.getId());

        harness.castFromHand(player1, new VexilusPraetor(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vexilus Praetor");
        harness.assertOnBattlefield(player1, "Inquisitor Greyfax");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void protectionDoesNotPreventUntargetedDestruction() {
        harness.addToBattlefield(player1, new VexilusPraetor());
        Permanent commander = harness.addToBattlefieldAndReturn(player1, new InquisitorGreyfax());
        gd.makeCommander(player1.getId(), commander.getCard());
        assertThat(gqs.hasProtectionFromSource(gd, commander, new Exterminatus())).isTrue();

        harness.castFromHand(player1, new Exterminatus(), "{5}{W}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Inquisitor Greyfax");
        harness.assertNotOnBattlefield(player1, "Vexilus Praetor");
    }
}
