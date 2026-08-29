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
        addReadyCatOwl();
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
        addReadyCatOwl();
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
        addReadyCatOwl();
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        declareAttackers(player1, List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    private Permanent addReadyCatOwl() {
        Permanent catOwl = harness.addToBattlefieldAndReturn(player1, new CatOwl());
        catOwl.setSummoningSick(false);
        return catOwl;
    }
}
