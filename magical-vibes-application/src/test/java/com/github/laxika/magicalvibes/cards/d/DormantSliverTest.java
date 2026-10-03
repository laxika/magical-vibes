package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.cards.s.SinewSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DormantSliver.class, SinewSliver.class, HedgeTroll.class,
        ArtificialEvolution.class, Bitterblossom.class})
class DormantSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Grants defender to all Slivers, including opponents' Slivers")
    void grantsDefenderToAllSlivers() {
        addCreatureReady(player1, new DormantSliver());
        Permanent ownSliver = addCreatureReady(player1, new SinewSliver());
        Permanent opposingSliver = addCreatureReady(player2, new SinewSliver());
        Permanent nonSliver = addCreatureReady(player1, new HedgeTroll());

        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonSliver, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Grants defender to Dormant Sliver itself")
    void grantsDefenderToSelf() {
        Permanent dormantSliver = addCreatureReady(player1, new DormantSliver());

        assertThat(gqs.hasKeyword(gd, dormantSliver, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("A Dormant Sliver draws a card when it enters")
    void drawsWhenItEnters() {
        harness.setLibrary(player1, List.of(new HedgeTroll()));
        harness.castFromHand(player1, new DormantSliver(), "{2}{G}{U}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hedge Troll");
    }

    @Test
    @DisplayName("A Sliver entering under any player's control draws for that player")
    void drawsForSliverEnteringUnderOpponentsControl() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setLibrary(player2, List.of(new HedgeTroll()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new SinewSliver(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hedge Troll");
    }

    @Test
    @DisplayName("A non-Sliver entering does not draw a card")
    void doesNotDrawForNonSliver() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setLibrary(player1, List.of(new SinewSliver()));
        harness.castFromHand(player1, new HedgeTroll(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Dormant Sliver grants a separate draw trigger")
    void drawsOnceForEachDormantSliver() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.addToBattlefield(player2, new DormantSliver());
        harness.setLibrary(player1, List.of(new HedgeTroll(), new SinewSliver()));

        harness.castFromHand(player1, new DormantSliver(), "{2}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("A Sliver draws once for each Dormant Sliver, for its own controller")
    void resolvesMultipleGrantedDrawTriggers() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.addToBattlefield(player2, new DormantSliver());
        harness.setLibrary(player1, List.of(new HedgeTroll(), new SinewSliver()));

        harness.castFromHand(player1, new SinewSliver(), "{1}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hedge Troll", "Sinew Sliver");
    }

    @Test
    @DisplayName("A noncreature Sliver permanent draws when it enters")
    void noncreatureSliverDrawsWhenItEnters() {
        harness.addToBattlefield(player1, new DormantSliver());
        harness.setLibrary(player1, List.of(new HedgeTroll()));
        harness.setHand(player1, List.of(new Bitterblossom(), new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0);
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "SLIVER");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Bitterblossom");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Hedge Troll");
    }
}
