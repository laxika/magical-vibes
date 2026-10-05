package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Frogmite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HareApparent;
import com.github.laxika.magicalvibes.cards.h.HeapedHarvest;
import com.github.laxika.magicalvibes.cards.r.RaccoonRallier;
import com.github.laxika.magicalvibes.cards.s.SquirrelMob;
import com.github.laxika.magicalvibes.cards.t.ThornvaultForager;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakhollowVillage.class, Frogmite.class, HareApparent.class, RaccoonRallier.class,
        SquirrelMob.class, GrizzlyBears.class, ThornvaultForager.class, HeapedHarvest.class})
class OakhollowVillageTest extends BaseCardTest {

    @Test
    void tapsForColorless() {
        addVillage();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void addsGreenManaOnlyForCreatureSpells() {
        addVillage();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    void putsCountersOnQualifyingPermanentsThatEnteredThisTurn() {
        addVillage();

        Permanent oldFrog = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        Permanent newFrog = harness.addToBattlefieldAndReturn(player1, new Frogmite());
        Permanent newRabbit = harness.addToBattlefieldAndReturn(player1, new HareApparent());
        Permanent newRaccoon = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        Permanent newSquirrel = harness.addToBattlefieldAndReturn(player1, new SquirrelMob());
        Permanent newBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentFrog = harness.addToBattlefieldAndReturn(player2, new Frogmite());

        gd.permanentsEnteredBattlefieldThisTurn.put(
                player1.getId(), new ArrayList<>(List.of(
                        newFrog.getCard(), newRabbit.getCard(), newRaccoon.getCard(), newSquirrel.getCard())));
        gd.permanentsEnteredBattlefieldThisTurn.put(
                player2.getId(), new ArrayList<>(List.of(opponentFrog.getCard())));

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(oldFrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(newFrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newRabbit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newRaccoon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newSquirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentFrog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void restrictedGreenPaysForCreatureSpell() {
        addVillage();
        harness.setHand(player1, List.of(new ThornvaultForager()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ThornvaultForager);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isZero();
    }

    @Test
    void restrictedGreenCannotPayForNoncreatureSpell() {
        addVillage();
        harness.setHand(player1, List.of(new HeapedHarvest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void restrictedGreenCannotPayForCounterAbility() {
        addVillage();
        addVillage();
        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();
    }

    @Test
    void includesQualifyingPermanentEnteringInResponse() {
        addVillage();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 2, null, null);
        assertThat(gd.stack).hasSize(1);

        Permanent squirrel = harness.enterBattlefieldAndReturn(player1, new ThornvaultForager());
        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(squirrel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterAbilityResolvesWithoutQualifyingPermanents() {
        addVillage();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void addVillage() {
        harness.addToBattlefield(player1, new OakhollowVillage());
    }
}
