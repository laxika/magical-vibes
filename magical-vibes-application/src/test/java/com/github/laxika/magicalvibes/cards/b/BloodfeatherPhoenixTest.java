package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FlamesOfTheFirebrand;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolarBlast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodfeatherPhoenix.class, Shock.class, GrizzlyBears.class, InvasionOfInnistrad.class,
        FlamesOfTheFirebrand.class, SolarBlast.class})
class BloodfeatherPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns from the graveyard when a spell damages an opponent and gains haste")
    void returnsWhenSpellDamagesOpponent() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent phoenix = findPermanent(player1, "Bloodfeather Phoenix");
        assertThat(phoenix.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Bloodfeather Phoenix");
    }

    @Test
    @DisplayName("Returns from the graveyard when a spell damages a battle")
    void returnsWhenSpellDamagesBattle() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);

        harness.castAndResolveInstant(player1, 0, battle.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Bloodfeather Phoenix");
    }

    @Test
    @DisplayName("Does not return when the spell damages a creature")
    void doesNotReturnWhenSpellDamagesCreature() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
    }

    @Test
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player1, new BloodfeatherPhoenix());
        addCreatureReady(player2, new BloodfeatherPhoenix());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayDeclinePayment() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
        harness.assertNotOnBattlefield(player1, "Bloodfeather Phoenix");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void cannotReturnWithoutRedMana() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
        harness.assertNotOnBattlefield(player1, "Bloodfeather Phoenix");
    }

    @Test
    void doesNotTriggerForDamageToYourself() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
    }

    @Test
    void opponentsSpellDoesNotTriggerYourPhoenix() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
    }

    @Test
    void hasteExpiresAtEndOfTurnAndPaymentConsumesRedMana() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent phoenix = findPermanent(player1, "Bloodfeather Phoenix");
        assertThat(gqs.hasKeyword(gd, phoenix, Keyword.HASTE)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, phoenix, Keyword.HASTE)).isFalse();
        harness.assertOnBattlefield(player1, "Bloodfeather Phoenix");
    }

    @Test
    void eachPhoenixReturnsOnlyItselfForItsOwnPayment() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix(), new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Bloodfeather Phoenix")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Bloodfeather Phoenix")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
    }

    @Test
    void simultaneousDamageToOpponentAndBattleTriggersTwice() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, Map.of(player2.getId(), 1, battle.getId(), 2));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Bloodfeather Phoenix");
    }

    @Test
    void dyingFromSameDamageEventDoesNotTrigger() {
        Permanent phoenix = harness.addToBattlefieldAndReturn(player1, new BloodfeatherPhoenix());
        harness.setHand(player1, List.of(new FlamesOfTheFirebrand()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, Map.of(phoenix.getId(), 2, player2.getId(), 1));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cyclingDamageFromAnInstantCardIsNotSpellDamage() {
        harness.setGraveyard(player1, List.of(new BloodfeatherPhoenix()));
        harness.setHand(player1, List.of(new SolarBlast()));
        harness.setLibrary(player1, List.of(new BloodfeatherPhoenix()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfeather Phoenix");
        harness.assertInHand(player1, "Bloodfeather Phoenix");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
