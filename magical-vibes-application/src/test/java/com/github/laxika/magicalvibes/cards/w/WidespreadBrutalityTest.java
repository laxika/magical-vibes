package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GoblinAssailant;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.s.SorinVengefulBloodlord;
import com.github.laxika.magicalvibes.cards.v.VizierOfTheScorpion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WidespreadBrutality.class, GoblinAssailant.class, JayaVeneratedFiremage.class,
        PrimordialWurm.class, SorinVengefulBloodlord.class, VizierOfTheScorpion.class})
class WidespreadBrutalityTest extends BaseCardTest {

    @Test
    @DisplayName("Amasses a 2/2 Army and damages each non-Army creature")
    void amassesAndDamagesNonArmyCreatures() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());

        castWidespreadBrutality();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getEffectivePower()).isEqualTo(2);
        assertThat(army.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponent.getId()));
    }

    @Test
    @DisplayName("Adds counters to an existing Army before using its power")
    void amassesOnExistingArmyBeforeDamaging() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());

        castWidespreadBrutality();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(opponent.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void damagesFriendlyNonArmiesButExemptsOpposingArmies() {
        harness.addToBattlefield(player1, new GoblinAssailant());
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player2, new GoblinAssailant());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        castWidespreadBrutality();

        harness.assertInGraveyard(player1, "Goblin Assailant");
        harness.assertOnBattlefield(player2, "Goblin Assailant");
        assertThat(opposingArmy.getMarkedDamage()).isZero();
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(wurm.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void usesOnlyTheChosenArmyWhenMultipleArmiesExist() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new GoblinAssailant());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        chosen.getGrantedSubtypes().add(CardSubtype.ARMY);
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        castWidespreadBrutality();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(opponent.getMarkedDamage()).isZero();
        harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(chosen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(chosen.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(first.getMarkedDamage()).isZero();
        assertThat(chosen.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void blackArmyDamageDoesNotReceiveJayasRedSourceBonus() {
        harness.addToBattlefield(player1, new JayaVeneratedFiremage());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());

        castWidespreadBrutality();

        assertThat(opponent.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void armyLifelinkGainsLifeForDamageToEveryNonArmyCreature() {
        harness.addToBattlefield(player1, new SorinVengefulBloodlord());
        harness.addToBattlefield(player1, new GoblinAssailant());
        harness.addToBattlefield(player2, new PrimordialWurm());

        castWidespreadBrutality();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
    }

    @Test
    void armyDeathtouchAppliesEvenWhenItsGrantingCreatureDiesFromTheSameDamage() {
        harness.addToBattlefield(player1, new VizierOfTheScorpion());
        harness.addToBattlefield(player2, new PrimordialWurm());

        castWidespreadBrutality();

        harness.assertInGraveyard(player1, "Vizier of the Scorpion");
        harness.assertInGraveyard(player2, "Primordial Wurm");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private void castWidespreadBrutality() {
        harness.castFromHand(player1, new WidespreadBrutality(), "{1}{B}{R}{R}");
        harness.passBothPriorities();
    }
}
