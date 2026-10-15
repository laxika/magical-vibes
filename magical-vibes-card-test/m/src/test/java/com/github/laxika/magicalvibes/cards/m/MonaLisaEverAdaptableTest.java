package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonaLisaEverAdaptable.class, GrizzlyBears.class, LightningBolt.class})
class MonaLisaEverAdaptableTest extends BaseCardTest {

    @Test
    void createsMutagenWhenYouCastCreatureSpell() {
        harness.addToBattlefield(player1, new MonaLisaEverAdaptable());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void createsMutagenWhenOpponentCastsCreatureSpell() {
        harness.addToBattlefield(player1, new MonaLisaEverAdaptable());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(findPermanents(player2, "Mutagen")).isEmpty();
        harness.passBothPriorities();
    }

    @Test
    void doesNotCreateMutagenForNoncreatureSpell() {
        harness.addToBattlefield(player1, new MonaLisaEverAdaptable());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void mutagenPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new MonaLisaEverAdaptable());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mutagen = findPermanent(player1, "Mutagen");
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0,
                null, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void createdTokenHasMutagenSubtype() {
        Permanent mutagen = createMutagen();

        assertThat(mutagen.getCard().getSubtypes()).contains(CardSubtype.MUTAGEN);
    }

    @Test
    void doesNotTriggerForItsOwnCreatureSpell() {
        harness.castFromHand(player1, new MonaLisaEverAdaptable(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void enteringCreatureWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new MonaLisaEverAdaptable());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void mutagenCanTargetOpponentsCreatureAndIsSacrificedAsCost() {
        Permanent mutagen = createMutagen();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen),
                0, null, creature.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCannotActivateOutsideMainPhase() {
        Permanent mutagen = createMutagen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, null,
                findPermanent(player1, "Mona Lisa, Ever Adaptable").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenCannotActivateDuringOpponentsTurn() {
        Permanent mutagen = createMutagen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, null,
                findPermanent(player1, "Mona Lisa, Ever Adaptable").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenCannotActivateWithSpellOnStack() {
        Permanent mutagen = createMutagen();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, null,
                findPermanent(player1, "Mona Lisa, Ever Adaptable").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void tappedMutagenCannotActivate() {
        Permanent mutagen = createMutagen();
        mutagen.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, null,
                findPermanent(player1, "Mona Lisa, Ever Adaptable").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void mutagenRequiresOneMana() {
        Permanent mutagen = createMutagen();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0, null,
                findPermanent(player1, "Mona Lisa, Ever Adaptable").getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    private Permanent createMutagen() {
        harness.addToBattlefield(player1, new MonaLisaEverAdaptable());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Mutagen");
    }
}
