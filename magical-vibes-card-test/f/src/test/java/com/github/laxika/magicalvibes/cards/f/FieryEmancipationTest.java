package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({FieryEmancipation.class, GrizzlyBears.class, SerraAngel.class, Shock.class, HealingSalve.class})
class FieryEmancipationTest extends BaseCardTest {

    @Test
    @DisplayName("Triples damage from a controlled spell to a player")
    void triplesControlledSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Triples damage from a controlled spell to a permanent")
    void triplesControlledSpellDamageToPermanent() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID serraId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveInstant(player1, 0, serraId);

        harness.assertInGraveyard(player2, "Serra Angel");
    }

    @Test
    @DisplayName("Triples controlled combat damage")
    void triplesControlledCombatDamage() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not amplify an opponent's damage")
    void doesNotAmplifyOpponentsDamage() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Two copies multiply damage ninefold")
    void multipleCopiesStackMultiplicatively() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 2);
    }

    @Test
    @DisplayName("Triples controlled damage even when it hits its controller")
    void triplesDamageToSelf() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Triples controlled damage to a friendly permanent")
    void triplesDamageToFriendlyPermanent() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Affected player chooses between tripling and finite prevention first")
    void offersReplacementOrderForFinitePrevention() {
        harness.addToBattlefield(player1, new FieryEmancipation());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
    }
}
