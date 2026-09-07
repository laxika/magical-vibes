package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrangMasterMind.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class KrangMasterMindTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws up to four cards when fewer than four remain in hand")
    void entersAndDrawsDifferenceToFour() {
        harness.setHand(player1, List.of(new KrangMasterMind(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addKrangMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does not draw when four or more cards remain in hand")
    void doesNotDrawWithFourCardsInHand() {
        harness.setHand(player1, List.of(
                new KrangMasterMind(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addKrangMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Gets +1/+0 for each other artifact controlled")
    void boostsForOtherArtifactsYouControl() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangMasterMind());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, krang)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, krang)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count itself for the artifact boost")
    void doesNotCountItself() {
        Permanent krang = harness.addToBattlefieldAndReturn(player1, new KrangMasterMind());

        assertThat(gqs.getEffectivePower(gd, krang)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, krang)).isEqualTo(4);
    }

    private void addKrangMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }
}
