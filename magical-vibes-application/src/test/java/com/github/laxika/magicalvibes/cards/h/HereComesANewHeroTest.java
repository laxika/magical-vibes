package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HereComesANewHero.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class HereComesANewHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws X cards and the spell controller creates a creature copy")
    void drawsAndCreatesTokenCopy() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(2, player2.getId(), creature.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears")
                        && permanent.getCard().getPower() == 2
                        && permanent.getCard().getToughness() == 2);
    }

    @Test
    @DisplayName("The creature copy target is optional")
    void mayDeclineCreatureTarget() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(2, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("The creature target must have mana value X or less")
    void rejectsCreatureAboveManaValueLimit() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new HereComesANewHero()));
        addManaForX(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X may be zero without drawing any cards")
    void zeroDrawsNoCards() {
        harness.setLibrary(player2, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player2.getId()).size();

        cast(0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Here Comes a New Hero!");
    }

    @Test
    @DisplayName("The controller may draw and copy their own creature with mana value below X")
    void copiesOwnCreatureWithoutCountersOrTappedStatus() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        creature.tap();

        cast(3, player1.getId(), creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears");
                    assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                    assertThat(token.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("A low mana value noncreature is not a legal copy target")
    void rejectsNoncreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new HereComesANewHero()));
        addManaForX(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2,
                List.of(player2.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The player target cannot be omitted")
    void requiresPlayerTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HereComesANewHero()));
        addManaForX(2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal creature target does not prevent the legal player from drawing")
    void drawsWhenCreatureLeavesBeforeResolution() {
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.setHand(player1, List.of(new HereComesANewHero()));
        addManaForX(2);
        harness.castSorcery(player1, 0, 2, List.of(player2.getId(), creature.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Here Comes a New Hero!");
    }

    private void cast(int xValue, java.util.UUID... targets) {
        harness.setHand(player1, List.of(new HereComesANewHero()));
        addManaForX(xValue);
        harness.castSorcery(player1, 0, xValue, List.of(targets));
        harness.passBothPriorities();
    }

    private void addManaForX(int xValue) {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue + 2);
    }
}
