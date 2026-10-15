package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingedBoots.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class WingedBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has flying and ward {4}")
    void equippedCreatureGetsFlyingAndWard() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        boots.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying ward {4} lets an opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        boots.setAttachedTo(bears.getId());

        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castInstant(player2, 0, bears.getId());
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(4);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip ability attaches Winged Boots to a creature")
    void equipAttachesBoots() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    void controllerSpellDoesNotTriggerWard() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        boots.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void opponentActivatedAbilityIsCounteredWithoutPayment() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        boots.setAttachedTo(bears.getId());
        addCreatureReady(player2, new ProdigalPyromancer());
        prepareOpponentTurn();

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void opponentCanDeclineWardDespiteHavingEnoughMana() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        boots.setAttachedTo(bears.getId());
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    void payingWardAllowsOpponentActivatedAbilityToResolve() {
        Permanent boots = addReadyBoots(player1);
        Permanent bears = addCreatureReady(player1);
        boots.setAttachedTo(bears.getId());
        addCreatureReady(player2, new ProdigalPyromancer());
        prepareOpponentTurn();
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }

    @Test
    void movingBootsTransfersFlyingAndWard() {
        Permanent boots = addReadyBoots(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        boots.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
        prepareOpponentTurn();
        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, first.getId());
        assertThat(gqs.findPermanentById(gd, first.getId())).isNull();
        harness.castAndResolveInstant(player2, 0, second.getId());
        assertThat(gqs.findPermanentById(gd, second.getId())).isNotNull();
        assertThat(second.getMarkedDamage()).isZero();
    }

    private Permanent addReadyBoots(Player player) {
        Permanent boots = harness.addToBattlefieldAndReturn(player, new WingedBoots());
        boots.setSummoningSick(false);
        return boots;
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
