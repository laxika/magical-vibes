package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AnointedProcession;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnterTheGodEternals.class, GrizzlyBears.class, HealingSalve.class,
        PrimordialWurm.class, AnointedProcession.class, MentorOfTheMeek.class})
class EnterTheGodEternalsTest extends BaseCardTest {

    @Test
    @DisplayName("deals 4 damage, gains life equal to damage dealt, mills four, and amasses four")
    void resolvesAllEffects() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, bear);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        castEnterTheGodEternals(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 4);
        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("gains life only for damage that is not prevented")
    void lifeGainUsesActualDamage() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, bear);

        harness.setHand(player1, List.of(new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castEnterTheGodEternals(target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("adds counters to an existing Army instead of creating a token")
    void amassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        GrizzlyBears targetCard = new GrizzlyBears();
        targetCard.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        castEnterTheGodEternals(target);

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
    }

    @Test
    void requiresAcreatureAndPlayerTarget() {
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId(), List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secondTargetMustBeAPlayer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fullyPreventedDamageStillMillsAndAmassesWithoutLifeGain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new HealingSalve(), new HealingSalve()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, 1, target.getId());
        harness.passBothPriorities();

        castEnterTheGodEternals(target);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(4));
    }

    @Test
    void stillMillsAndAmassesWhenCreatureTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();
        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize - 4);
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(4));
    }

    @Test
    void canMillControllerWithFewerThanFourCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId(), player1.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void lethalDamageToOwnArmyIsFollowedByCountersBeforeItCanDie() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castEnterTheGodEternals(army);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(army);
        assertThat(army.getMarkedDamage()).isEqualTo(4);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void choosesOneOfMultipleExistingArmies() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        castEnterTheGodEternals(target);
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void tokenDoublingStillPutsCountersOnOnlyOneArmy() {
        harness.addToBattlefield(player1, new AnointedProcession());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        castEnterTheGodEternals(target);
        if (gd.interaction.isAwaitingInput()) {
            Permanent chosen = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(permanent -> permanent.getCard().isToken())
                    .findFirst().orElseThrow();
            harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));
        }

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement()
                .satisfies(army -> assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(4));
    }

    @Test
    void newArmyEntersWithZeroPowerAndTriggersMentor() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        castEnterTheGodEternals(target);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castEnterTheGodEternals(Permanent creatureTarget) {
        harness.setHand(player1, List.of(new EnterTheGodEternals()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, List.of(creatureTarget.getId(), player2.getId()));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
