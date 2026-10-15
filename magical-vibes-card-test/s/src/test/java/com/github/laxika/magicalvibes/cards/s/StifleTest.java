package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.d.DecreeOfSilence;
import com.github.laxika.magicalvibes.cards.e.ElvishAberration;
import com.github.laxika.magicalvibes.cards.k.KrosanWarchief;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stifle.class, KrosanWarchief.class, DecreeOfSilence.class, AvenFarseer.class,
        ElvishAberration.class})
class StifleTest extends BaseCardTest {

    @Test
    void countersAnActivatedAbility() {
        Permanent warchief = addCreatureReady(player2, new KrosanWarchief());
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, warchief.getId());
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, warchief.getCard().getId());

        assertThat(warchief.getRegenerationShield()).isZero();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    void countersATriggeredAbility() {
        Permanent decree = harness.addToBattlefieldAndReturn(player1, new DecreeOfSilence());
        harness.setHand(player2, List.of(new AvenFarseer()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getCard().getId());

        assertThat(decree.getCounterCount(CounterType.DEPLETION)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void countersOnlyTheSelectedAbilityFromTheSameSource() {
        Permanent warchief = addCreatureReady(player2, new KrosanWarchief());
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, warchief.getId());
        var remainingAbilityId = gd.stack.getFirst().getTargetableId();
        harness.activateAbility(player2, 0, 0, warchief.getId());
        var targetedAbilityId = gd.stack.getLast().getTargetableId();
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, targetedAbilityId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(remainingAbilityId);
        assertThat(warchief.getRegenerationShield()).isZero();
        harness.assertOnBattlefield(player2, "Krosan Warchief");
        harness.assertNotInGraveyard(player2, "Krosan Warchief");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(warchief.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetASpell() {
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new AvenFarseer()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gd.stack.getLast().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetAManaAbility() {
        Permanent aberration = addCreatureReady(player2, new ElvishAberration());
        harness.setHand(player1, List.of(new Stifle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();

        harness.passPriority(player2);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, aberration.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
