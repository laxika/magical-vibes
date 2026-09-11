package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BronzeSword;
import com.github.laxika.magicalvibes.cards.d.DwarvenPony;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KLiTheResourceful.class, BronzeSword.class, DwarvenPony.class,
        Forest.class, FountainOfYouth.class, GrizzlyBears.class})
class KLiTheResourcefulTest extends BaseCardTest {

    @Test
    void drawsOnlyOnceWhenAnotherDwarfAndEquipmentEnter() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());

        harness.enterBattlefieldAndReturn(player1, new DwarvenPony());
        harness.enterBattlefieldAndReturn(player1, new BronzeSword());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawWhenKiliEnters() {
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new KLiTheResourceful());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void firstEquipEachTurnIsFreeAfterEnduringStory() {
        addStoriedKili();
        Permanent firstSword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent secondSword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, battlefieldIndex(firstSword), null, creature.getId());
        harness.passBothPriorities();

        assertThat(firstSword.getAttachedTo()).isEqualTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(secondSword), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void equipIsNotFreeBeforeEnduringStory() {
        harness.addToBattlefieldAndReturn(player1, new KLiTheResourceful());
        Permanent sword = harness.addToBattlefieldAndReturn(player1, new BronzeSword());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(sword), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void addStoriedKili() {
        harness.enterBattlefieldAndReturn(player1, new KLiTheResourceful());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gd.playersWithEnduringStory).contains(player1.getId());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
