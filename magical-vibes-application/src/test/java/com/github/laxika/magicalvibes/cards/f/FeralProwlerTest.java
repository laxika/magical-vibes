package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PuncturingBlow;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeralProwler.class, GrizzlyBears.class, WrathOfGod.class, PuncturingBlow.class, Unsummon.class})
class FeralProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Feral Prowler dies from Wrath of God, draws a card")
    void diesFromWrathOfGodDrawsCard() {
        harness.addToBattlefield(player1, new FeralProwler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Feral Prowler");
        harness.assertInGraveyard(player1, "Feral Prowler");

        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Feral Prowler"));

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Each Prowler dying simultaneously draws one card for its controller")
    void simultaneousDeathsDrawForEachController() {
        harness.addToBattlefield(player1, new FeralProwler());
        harness.addToBattlefield(player1, new FeralProwler());
        harness.addToBattlefield(player2, new FeralProwler());
        harness.setLibrary(player1, List.of(new FeralProwler(), new FeralProwler(), new FeralProwler()));
        harness.setLibrary(player2, List.of(new FeralProwler(), new FeralProwler()));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exile instead of dying does not trigger the draw")
    void exileReplacementDoesNotDraw() {
        var prowler = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player1, List.of(new PuncturingBlow()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new FeralProwler(), new FeralProwler()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, prowler.getId());

        harness.assertNotOnBattlefield(player2, "Feral Prowler");
        assertThat(gd.findExiledCard(prowler.getCard().getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Prowler to hand does not trigger the draw")
    void returningToHandDoesNotDraw() {
        var prowler = harness.addToBattlefieldAndReturn(player2, new FeralProwler());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new FeralProwler(), new FeralProwler()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, prowler.getId());

        harness.assertNotOnBattlefield(player2, "Feral Prowler");
        harness.assertInHand(player2, "Feral Prowler");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
