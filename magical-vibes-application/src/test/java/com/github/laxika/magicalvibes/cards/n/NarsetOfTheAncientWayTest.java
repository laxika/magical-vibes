package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetOnControllerSpellCastEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarsetOfTheAncientWay.class, GrizzlyBears.class, Mountain.class, Shock.class})
class NarsetOfTheAncientWayTest extends BaseCardTest {

    @Test
    void plusOneGainsLifeAndAddsRestrictedMana() {
        Permanent narset = addReadyNarset(player1, 4);
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("BLUE", "RED", "WHITE");

        harness.handleListChoice(player1, "BLUE");

        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).getNoncreatureSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void plusOneManaCanCastNoncreatureSpellsButNotCreatures() {
        addReadyNarset(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void minusTwoDrawsAndDamagesForDiscardedNonlandManaValue() {
        Permanent narset = addReadyNarset(player1, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new Shock();
        Card drawn = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(narset.getCard(), discarded);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void minusTwoDoesNotDamageForDiscardedLand() {
        addReadyNarset(player1, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card discarded = new Mountain();
        Card drawn = new Mountain();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void minusSixCreatesEmblemThatDamagesOnNoncreatureSpellCast() {
        Permanent narset = addReadyNarset(player1, 6);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(narset.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects()).singleElement()
                .isInstanceOf(DealDamageToAnyTargetOnControllerSpellCastEffect.class);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    void minusSixEmblemDoesNotTriggerOnCreatureSpell() {
        addReadyNarset(player1, 6);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyNarset(Player player, int loyalty) {
        Permanent narset = new Permanent(new NarsetOfTheAncientWay());
        narset.setCounterCount(CounterType.LOYALTY, loyalty);
        narset.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(narset);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return narset;
    }
}
