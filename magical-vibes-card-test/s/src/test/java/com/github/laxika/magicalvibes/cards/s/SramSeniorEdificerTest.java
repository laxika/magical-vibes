package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.m.MobileGarrison;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SramSeniorEdificer.class, GrizzlyBears.class, HolyStrength.class, Bonesplitter.class,
        SkySkiff.class, MobileGarrison.class})
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

    @Test
    @DisplayName("The draw resolves before the qualifying spell and survives Sram leaving")
    void drawResolvesBeforeSpellAfterSramLeaves() {
        var sram = harness.addToBattlefieldAndReturn(player1, new SramSeniorEdificer());
        Card drawn = new SramSeniorEdificer();
        harness.setHand(player1, List.of(new MobileGarrison()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(2);
        gd.playerBattlefields.get(player1.getId()).remove(sram);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertNotOnBattlefield(player1, "Mobile Garrison");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mobile Garrison");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("An opponent casting a qualifying spell does not trigger Sram")
    void opponentsVehicleDoesNotDrawCard() {
        harness.addToBattlefield(player1, new SramSeniorEdificer());
        Card drawn = new SramSeniorEdificer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new MobileGarrison()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mobile Garrison");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A Vehicle entering without being cast does not trigger Sram")
    void vehicleEnteringWithoutCastDoesNotDrawCard() {
        harness.addToBattlefield(player1, new SramSeniorEdificer());
        Card drawn = new SramSeniorEdificer();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        harness.enterBattlefieldAndReturn(player1, new MobileGarrison());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mobile Garrison");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }
}
