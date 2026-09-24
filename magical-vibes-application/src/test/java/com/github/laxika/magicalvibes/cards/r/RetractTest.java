package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.m.Memnarch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Retract.class, DarksteelIngot.class, DarksteelCitadel.class, CrazedGoblin.class, Memnarch.class})
class RetractTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all artifacts you control to their owners' hands")
    void returnsAllArtifactsYouControl() {
        harness.addToBattlefield(player1, new DarksteelIngot());
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player1, new CrazedGoblin());
        harness.addToBattlefield(player2, new DarksteelIngot());
        harness.setHand(player1, List.of(new Retract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Crazed Goblin");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Darksteel Ingot");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Darksteel Ingot", "Darksteel Citadel");
    }

    @Test
    @DisplayName("Resolves when you control no artifacts")
    void resolvesWithNoArtifacts() {
        harness.addToBattlefield(player1, new CrazedGoblin());
        harness.setHand(player1, List.of(new Retract()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crazed Goblin");
        harness.assertInGraveyard(player1, "Retract");
    }

    @Test
    @DisplayName("Returns an artifact you control even when another player owns it")
    void returnsControlledArtifactToItsOwnersHand() {
        Permanent memnarch = addCreatureReady(player1, new Memnarch());
        Permanent stolenArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, stolenArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(memnarch, stolenArtifact);

        harness.setHand(player1, List.of(new Retract()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInHand(player1, "Memnarch");
        harness.assertInHand(player2, "Darksteel Ingot");
        harness.assertInGraveyard(player1, "Retract");
    }
}
