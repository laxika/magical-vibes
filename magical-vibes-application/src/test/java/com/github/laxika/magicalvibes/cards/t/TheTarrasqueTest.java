package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheTarrasque.class, HillGiant.class, Shock.class})
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
        Permanent tarrasque = new Permanent(new TheTarrasque());
        gd.playerBattlefields.get(player1.getId()).add(tarrasque);

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

    private Permanent castTarrasque(Player player) {
        harness.setHand(player, List.of(new TheTarrasque()));
        harness.addMana(player, ManaColor.COLORLESS, 6);
        harness.addMana(player, ManaColor.GREEN, 3);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        return findPermanent(player, "The Tarrasque");
    }
}
