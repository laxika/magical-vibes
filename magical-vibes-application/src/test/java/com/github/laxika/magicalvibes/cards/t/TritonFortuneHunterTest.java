package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CoordinatedAssault;
import com.github.laxika.magicalvibes.cards.f.FeralInvocation;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TritonFortuneHunter.class, GiantGrowth.class, Shock.class, Forest.class,
        CoordinatedAssault.class, FeralInvocation.class})
class TritonFortuneHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Triton Fortune Hunter draws a card")
    void castingSpellThatTargetsHunterDrawsACard() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID hunterId = harness.getPermanentId(player1, "Triton Fortune Hunter");
        harness.castAndResolveInstant(player1, 0, hunterId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Triton Fortune Hunter")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell that targets Triton Fortune Hunter does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID hunterId = harness.getPermanentId(player1, "Triton Fortune Hunter");
        harness.castAndResolveInstant(player2, 0, hunterId);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void auraSpellDrawsBeforeItResolves() {
        UUID hunterId = harness.addToBattlefieldAndReturn(player1, new TritonFortuneHunter()).getId();
        harness.setHand(player1, List.of(new FeralInvocation()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, hunterId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Feral Invocation");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Feral Invocation");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void spellTargetingTwoHuntersDrawsOnceForEach() {
        UUID firstId = harness.addToBattlefieldAndReturn(player1, new TritonFortuneHunter()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player1, new TritonFortuneHunter()).getId();
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, List.of(firstId, secondId));
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroTargetSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new TritonFortuneHunter());
        harness.setHand(player1, List.of(new CoordinatedAssault()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void heroicStillDrawsAfterHunterIsKilledInResponse() {
        UUID hunterId = harness.addToBattlefieldAndReturn(player1, new TritonFortuneHunter()).getId();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.setHand(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hunterId);
        harness.castAndResolveInstant(player2, 0, hunterId);
        harness.assertInGraveyard(player1, "Triton Fortune Hunter");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Giant Growth");
        assertThat(gd.stack).isEmpty();
    }
}
