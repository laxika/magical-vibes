package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AfiyaGrove;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OzolithTheShatteredSpire.class, AfiyaGrove.class, DarksteelCitadel.class, GrizzlyBears.class})
class OzolithTheShatteredSpireTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling discards this card and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new OzolithTheShatteredSpire()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ozolith, the Shattered Spire");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not add a counter to a non-artifact, noncreature permanent")
    void doesNotModifyCountersOnOtherPermanents() {
        addOzolith(player1);

        harness.setHand(player1, List.of(new AfiyaGrove()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent grove = findPermanent(player1, "Afiya Grove");
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on a target creature")
    void putsTwoCountersOnTargetCreature() {
        addOzolith(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addOzolithMana();

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts two +1/+1 counters on a target artifact")
    void putsTwoCountersOnTargetArtifact() {
        addOzolith(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelCitadel());
        addOzolithMana();

        harness.activateAbility(player1, 0, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotTargetAnOpponentPermanent() {
        addOzolith(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addOzolithMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature you control");
    }

    @Test
    void abilityCanOnlyBeActivatedAsSorcery() {
        addOzolith(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addOzolithMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    private Permanent addOzolith(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new OzolithTheShatteredSpire());
    }

    private void addOzolithMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
