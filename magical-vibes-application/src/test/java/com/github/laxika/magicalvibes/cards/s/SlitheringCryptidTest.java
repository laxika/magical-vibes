package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlitheringCryptid.class})
class SlitheringCryptidTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Mutagen artifact token when it enters the battlefield")
    void createsMutagenWhenEnteringBattlefield() {
        assertThat(createMutagen()).isNotNull();
    }

    @Test
    @DisplayName("The Mutagen token puts a +1/+1 counter on a target creature")
    void mutagenPutsCounterOnCreature() {
        Permanent mutagen = createMutagen();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SlitheringCryptid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateMutagen(mutagen, creature);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tokenHasMutagenArtifactSubtype() {
        Permanent mutagen = createMutagen();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(mutagen.getCard().isToken()).isTrue();
        assertThat(mutagen.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(mutagen.getCard().getSubtypes()).contains(CardSubtype.MUTAGEN);
        assertThat(mutagen.getCard().getColors()).isEmpty();
        assertThat(mutagen.isTapped()).isFalse();
    }

    @Test
    void canTargetOpponentsCreatureAndSacrificesAsCost() {
        Permanent mutagen = createMutagen();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SlitheringCryptid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateMutagen(mutagen, creature);

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent mutagen = createMutagen();
        Permanent creature = findPermanent(player1, "Slithering Cryptid");

        assertThatThrownBy(() -> activateMutagen(mutagen, creature))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotActivateTappedToken() {
        Permanent mutagen = createMutagen();
        mutagen.setTapped(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activateMutagen(mutagen, findPermanent(player1, "Slithering Cryptid")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        Permanent mutagen = createMutagen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activateMutagen(mutagen, mutagen))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent mutagen = createMutagen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> activateMutagen(mutagen, findPermanent(player1, "Slithering Cryptid")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
    }

    @Test
    void cannotActivateDuringOpponentsTurn() {
        Permanent mutagen = createMutagen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> activateMutagen(mutagen, findPermanent(player1, "Slithering Cryptid")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
    }

    @Test
    void cannotActivateWithTriggerOnStack() {
        Permanent mutagen = createMutagen();
        harness.enterBattlefieldAndReturn(player1, new SlitheringCryptid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> activateMutagen(mutagen, findPermanent(player1, "Slithering Cryptid")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).containsExactly(mutagen);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Mutagen")).hasSize(2);
    }

    @Test
    void counterIsNotPutOnCreatureThatLeftBattlefield() {
        Permanent mutagen = createMutagen();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SlitheringCryptid());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        activateMutagen(mutagen, creature);
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent createMutagen() {
        harness.enterBattlefieldAndReturn(player1, new SlitheringCryptid());
        resolveAllTriggers();
        return findPermanent(player1, "Mutagen");
    }

    private void activateMutagen(Permanent mutagen, Permanent target) {
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                null, target.getId());
    }
}
