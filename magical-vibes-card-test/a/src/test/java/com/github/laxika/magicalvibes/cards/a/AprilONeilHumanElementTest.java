package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AprilONeilHumanElement.class, ChromaticStar.class, Divination.class, GrizzlyBears.class, Shock.class})
class AprilONeilHumanElementTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting an artifact creates a Mutagen for April's controller")
    void opponentArtifactSpellCreatesMutagenForController() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AprilONeilHumanElement());
        harness.setHand(player2, List.of(new ChromaticStar()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mutagen")).isEqualTo(1);
        assertThat(countPermanents(player2, "Mutagen")).isZero();
    }

    @Test
    @DisplayName("Instant and sorcery spells trigger, but creature spells do not")
    void matchingSpellTypesTrigger() {
        harness.addToBattlefield(player1, new AprilONeilHumanElement());
        harness.setHand(player1, List.of(new Shock(), new Divination(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mutagen")).isEqualTo(2);
    }

    @Test
    @DisplayName("A Mutagen can put a +1/+1 counter on a target creature")
    void mutagenPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new AprilONeilHumanElement());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent mutagen = findPermanent(player1, "Mutagen");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0,
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Mutagen")).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Created Mutagen tokens have the Mutagen artifact subtype")
    void createdTokenHasMutagenSubtype() {
        Permanent mutagen = createMutagen();

        assertThat(mutagen.getCard().getSubtypes()).extracting(Enum::name).contains("MUTAGEN");
    }

    @Test
    @DisplayName("A newly created Mutagen can target an opponent's creature")
    void mutagenCanTargetOpponentsCreatureImmediately() {
        Permanent mutagen = createMutagen();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0, null, creature.getId());

        assertThat(countPermanents(player1, "Mutagen")).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mutagen activation requires a main phase, the controller's turn, and an empty stack")
    void mutagenActivationRequiresSorceryTiming() {
        Permanent mutagen = createMutagen();
        Permanent creature = findPermanent(player1, "April O'Neil, Human Element");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mutagen);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mutagen.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Mutagen")).isEqualTo(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("The cast trigger creates a Mutagen even if April leaves before it resolves")
    void triggerSurvivesAprilsRemoval() {
        Permanent april = harness.addToBattlefieldAndReturn(player1, new AprilONeilHumanElement());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(april);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mutagen")).isEqualTo(1);
    }

    private Permanent createMutagen() {
        harness.addToBattlefield(player1, new AprilONeilHumanElement());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        return findPermanent(player1, "Mutagen");
    }
}
