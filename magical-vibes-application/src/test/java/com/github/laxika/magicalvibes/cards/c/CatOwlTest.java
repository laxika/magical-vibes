package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatOwl.class, GrizzlyBears.class, Island.class, Millstone.class})
class CatOwlTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking untaps target creature")
    void attackingUntapsTargetCreature() {
        addCreatureReady(player1, new CatOwl());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking untaps target artifact")
    void attackingUntapsTargetArtifact() {
        addCreatureReady(player1, new CatOwl());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        artifact.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a permanent that is neither an artifact nor a creature")
    void cannotTargetNonArtifactNonCreature() {
        addCreatureReady(player1, new CatOwl());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Cat-Owl can target itself and untaps only when its trigger resolves")
    void canUntapItself() {
        Permanent catOwl = addCreatureReady(player1, new CatOwl());

        declareAttackers(player1, List.of(0));
        assertThat(catOwl.isTapped()).isTrue();
        harness.handlePermanentChosen(player1, catOwl.getId());
        harness.passBothPriorities();

        assertThat(catOwl.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Attacking untaps only the chosen friendly creature")
    void untapsOnlyChosenFriendlyCreature() {
        Permanent attacker = addCreatureReady(player1, new CatOwl());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        target.tap();
        other.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(attacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An already untapped creature is a legal target")
    void canTargetUntappedCreature() {
        Permanent attacker = addCreatureReady(player1, new CatOwl());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CatOwl());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(attacker.isTapped()).isTrue();
    }
}
