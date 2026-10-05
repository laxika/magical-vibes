package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesWay.class, GrizzlyBears.class, LlanowarElves.class})
class NaturesWayTest extends BaseCardTest {

    @Test
    @DisplayName("Grants vigilance and trample, then deals power damage to an opponent creature")
    void grantsKeywordsAndDealsPowerDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId(), elvesId));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Granted keywords wear off at end of turn")
    void keywordsWearOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(bear.getId(),
                harness.getPermanentId(player2, "Llanowar Elves")));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Requires a creature you control first and a creature you do not control second")
    void validatesBothTargetRestrictions() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownElves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(ownBear.getId(), ownElves.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");

        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID opponentElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(opponentElvesId, ownElves.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot be cast without the second creature target")
    void requiresSecondTarget() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage is one-sided and equal to the source creature's power")
    void damageIsNotAFight() {
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(elves.getId(), bear.getId()));

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        assertThat(elves.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, elves, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Still grants keywords when only the opposing target leaves the battlefield")
    void grantsKeywordsWhenSecondTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(bear.getId(), elves.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(elves);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        harness.assertInGraveyard(player1, "Nature's Way");
    }

    @Test
    @DisplayName("Deals no damage when the source creature leaves before resolution")
    void noDamageWhenFirstTargetLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new NaturesWay()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, List.of(bear.getId(), elves.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(elves.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(gqs.hasKeyword(gd, elves, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Nature's Way");
    }
}
