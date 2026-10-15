package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonumentalCorruption.class, BottleGnomes.class, GrizzlyBears.class, Plains.class})
class MonumentalCorruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws and loses life equal to the artifacts controlled by the caster")
    void drawsAndLosesLifeBasedOnControllerArtifacts() {
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new MonumentalCorruption()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Draws no cards and causes no life loss with no artifacts")
    void doesNothingWithNoArtifacts() {
        harness.setHand(player1, List.of(new MonumentalCorruption()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MonumentalCorruption()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
