package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.y.YouComeToARiver;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheTarrasque.class, HillGiant.class, Shock.class, YouComeToARiver.class})
class TheTarrasqueTest extends BaseCardTest {

    @Test
    @DisplayName("A cast Tarrasque has haste and can attack immediately")
    void castTarrasqueHasHaste() {
        Permanent tarrasque = castTarrasque(player1);

        assertThat(gqs.hasKeyword(gd, tarrasque, Keyword.HASTE)).isTrue();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("A cast Tarrasque has ward 10")
    void castTarrasqueHasWard() {
        Permanent tarrasque = castTarrasque(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 11);

        harness.castInstant(player2, 0, tarrasque.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        assertThat(tarrasque.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A Tarrasque put onto the battlefield without being cast has neither conditional keyword ability")
    void putOntoBattlefieldWithoutCastingHasNoConditionalAbilities() {
        Permanent tarrasque = harness.addToBattlefieldAndReturn(player1, new TheTarrasque());

        assertThat(gqs.hasKeyword(gd, tarrasque, Keyword.HASTE)).isFalse();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 11);

        harness.castInstant(player2, 0, tarrasque.getId());
        harness.passBothPriorities();

        assertThat(tarrasque.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking makes The Tarrasque fight a creature defending player controls")
    void attacksAndFightsDefendingCreature() {
        Permanent tarrasque = castTarrasque(player1);
        Permanent giant = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(tarrasque.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature controlled by the attacker")
    void attackTriggerCannotTargetOwnCreature() {
        castTarrasque(player1);
        Permanent ownGiant = addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        Permanent tarrasque = castTarrasque(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 10);

        harness.castInstant(player2, 0, tarrasque.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(tarrasque.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        Permanent tarrasque = castTarrasque(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, tarrasque.getId());
        harness.passBothPriorities();

        assertThat(tarrasque.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void attackTriggerDoesNotFightWhenTarrasqueLeavesBattlefield() {
        Permanent tarrasque = castTarrasque(player1);
        Permanent giant = addCreatureReady(player2, new HillGiant());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());

        harness.setHand(player1, List.of(new YouComeToARiver()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, 0, tarrasque.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "The Tarrasque");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isZero();
    }

    @Test
    void uncastTarrasqueStillFightsWhenItAttacks() {
        Permanent tarrasque = addCreatureReady(player1, new TheTarrasque());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(tarrasque.getMarkedDamage()).isEqualTo(3);
    }

    private Permanent castTarrasque(Player player) {
        harness.castFromHand(player, new TheTarrasque(), "{6}{G}{G}{G}");
        harness.passBothPriorities();
        return findPermanent(player, "The Tarrasque");
    }
}
