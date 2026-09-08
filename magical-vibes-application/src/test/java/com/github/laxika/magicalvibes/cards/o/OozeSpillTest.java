package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OozeSpill.class, GrizzlyBears.class, Forest.class})
class OozeSpillTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndCreatesMutagen() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new OozeSpill()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player2, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenSacrificesForCounterOnTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        createMutagen();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mutagen = findPermanent(player2, "Mutagen");
        int mutagenIndex = gd.playerBattlefields.get(player2.getId()).indexOf(mutagen);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, mutagenIndex, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
    }

    @Test
    void mutagenCannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        createMutagen();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mutagen = findPermanent(player2, "Mutagen");
        int mutagenIndex = gd.playerBattlefields.get(player2.getId()).indexOf(mutagen);
        Permanent forest = findPermanent(player2, "Forest");
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, mutagenIndex, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void createMutagen() {
        GrizzlyBears spell = new GrizzlyBears();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new OozeSpill()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
    }
}
