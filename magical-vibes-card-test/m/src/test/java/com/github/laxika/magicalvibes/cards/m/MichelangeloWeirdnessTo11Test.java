package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DeathInTheFamily;
import com.github.laxika.magicalvibes.cards.z.ZooEscapees;
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

@CardUsed({MichelangeloWeirdnessTo11.class, ZooEscapees.class, Forest.class, DeathInTheFamily.class})
class MichelangeloWeirdnessTo11Test extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Mutagen token")
    void entersWithMutagenToken() {
        Permanent michelangelo = enterMichelangelo();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Mutagen puts an additional +1/+1 counter on a creature you control")
    void mutagenGetsAdditionalCounterOnOwnCreature() {
        enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        Permanent mutagen = findPermanent(player1, "Mutagen");

        activateMutagen(mutagen, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    @DisplayName("Mutagen puts only one counter on an opponent's creature")
    void mutagenDoesNotAddCounterToOpponentCreature() {
        enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZooEscapees());
        Permanent mutagen = findPermanent(player1, "Mutagen");

        activateMutagen(mutagen, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCanOnlyTargetCreatures() {
        enterMichelangelo();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                null,
                forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void mutagenCanOnlyBeActivatedAsSorcery() {
        enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                null,
                creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void replacementAppliesToMichelangeloHimself() {
        Permanent michelangelo = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");

        activateMutagen(mutagen, michelangelo.getId());

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mutagenIsSacrificedBeforeItsAbilityResolves() {
        Permanent michelangelo = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), null, michelangelo.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void mutagenRemainsUsableWithoutReplacementAfterMichelangeloLeaves() {
        Permanent michelangelo = enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.setHand(player1, List.of(new DeathInTheFamily()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, michelangelo.getId());
        harness.assertNotOnBattlefield(player1, "Michelangelo, Weirdness to 11");
        activateMutagen(mutagen, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void replacementUsesBattlefieldStateWhenMutagenResolves() {
        Permanent michelangelo = enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ZooEscapees());
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), null, creature.getId());
        harness.setHand(player2, List.of(new DeathInTheFamily()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, michelangelo.getId());
        harness.assertNotOnBattlefield(player1, "Michelangelo, Weirdness to 11");
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCannotBeActivatedWithASpellOnTheStack() {
        Permanent michelangelo = enterMichelangelo();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ZooEscapees());
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.setHand(player1, List.of(new DeathInTheFamily()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), null, michelangelo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
    }

    @Test
    void mutagenCannotBeActivatedDuringCombat() {
        Permanent michelangelo = enterMichelangelo();
        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), null, michelangelo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");

        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        assertThat(michelangelo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent enterMichelangelo() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent michelangelo = harness.enterBattlefieldAndReturn(player1, new MichelangeloWeirdnessTo11());
        resolveAllTriggers();
        return michelangelo;
    }

    private void activateMutagen(Permanent mutagen, java.util.UUID targetId) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                null,
                targetId);
        harness.passBothPriorities();
    }
}
