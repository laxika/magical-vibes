package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtlantisAttacks.class, Forest.class, GrizzlyBears.class})
class AtlantisAttacksTest extends BaseCardTest {

    @Test
    @DisplayName("Token mode creates a hexproof blue Leviathan for the target player")
    void createsLeviathanTokenForTargetPlayer() {
        harness.setHand(player1, List.of(new AtlantisAttacks()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(player2.getId()), null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(6);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(token.getCard().getColors()).containsExactly(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.LEVIATHAN);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Bounce mode returns one or two target nonland permanents")
    void returnsTwoNonlandPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtlantisAttacks()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(first.getId(), second.getId()), null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Bounce mode cannot target a land")
    void rejectsLandTarget() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new AtlantisAttacks()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(forest.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Teamwork requires both modes and taps creatures with total power at least four")
    void teamworkResolvesBothModes() {
        Permanent firstTapper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTapper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bounced = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtlantisAttacks()));
        addMana();

        harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 2, new int[]{0, 1},
                List.of(player2.getId(), bounced.getId()),
                List.of(firstTapper.getId(), secondTapper.getId()));
        harness.passBothPriorities();

        assertThat(firstTapper.isTapped()).isTrue();
        assertThat(secondTapper.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(permanent ->
                permanent.getId().equals(bounced.getId()));
        assertThat(gd.playerHands.get(player2.getId())).filteredOn(card -> card.getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent ->
                permanent.getCard().isToken() && permanent.getCard().getName().equals("Leviathan"));
    }

    @Test
    @DisplayName("Teamwork cannot be paid while choosing only one mode")
    void teamworkRequiresBothModes() {
        Permanent firstTapper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTapper = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AtlantisAttacks()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModesAndTaps(player1, 0, 1, 2,
                new int[]{0}, List.of(player2.getId()), List.of(firstTapper.getId(), secondTapper.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires choosing all modes");
        assertThat(firstTapper.isTapped()).isFalse();
        assertThat(secondTapper.isTapped()).isFalse();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
