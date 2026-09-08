package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SramSeniorEdificerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Aura spell draws a card")
    void auraSpellDrawsCard() {
        harness.addToBattlefield(player1, new SramSeniorEdificer());
        var target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Casting an Equipment spell draws a card")
    void equipmentSpellDrawsCard() {
        harness.addToBattlefield(player1, new SramSeniorEdificer());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new Bonesplitter()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Casting a Vehicle spell draws a card")
    void vehicleSpellDrawsCard() {
        harness.addToBattlefield(player1, new SramSeniorEdificer());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new SkySkiff()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Casting a non-Aura, non-Equipment, non-Vehicle spell does not draw")
    void otherSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new SramSeniorEdificer());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }
}
