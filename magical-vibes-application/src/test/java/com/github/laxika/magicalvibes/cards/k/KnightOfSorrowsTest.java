package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FinalPayment;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KnightOfSorrows.class, GrizzlyBears.class, WrathOfGod.class, FinalPayment.class})
class KnightOfSorrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Knight of Sorrows can block two attackers")
    void canBlockTwoAttackers() {
        Permanent knight = addCreatureReady(player2, new KnightOfSorrows());
        addAttackers(2);

        prepareDeclareBlockers();
        int knightIndex = gd.playerBattlefields.get(player2.getId()).indexOf(knight);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(knightIndex, 0),
                new BlockerAssignment(knightIndex, 1)
        ));

        assertThat(knight.isBlocking()).isTrue();
        assertThat(knight.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Knight of Sorrows cannot block three attackers")
    void cannotBlockThreeAttackers() {
        Permanent knight = addCreatureReady(player2, new KnightOfSorrows());
        addAttackers(3);

        prepareDeclareBlockers();
        int knightIndex = gd.playerBattlefields.get(player2.getId()).indexOf(knight);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(knightIndex, 0),
                new BlockerAssignment(knightIndex, 1),
                new BlockerAssignment(knightIndex, 2)
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Afterlife creates a 1/1 white and black Spirit token with flying")
    void afterlifeCreatesSpiritToken() {
        harness.addToBattlefield(player1, new KnightOfSorrows());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Knight of Sorrows");

        Permanent token = findPermanent(player1, "Spirit");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
            attacker.setAttacking(true);
        }
    }

    @Test
    void additionalBlockingAbilityDoesNotApplyToOtherCreatures() {
        addCreatureReady(player2, new KnightOfSorrows());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        addAttackers(2);
        prepareDeclareBlockers();
        int bearIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bear);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearIndex, 0),
                new BlockerAssignment(bearIndex, 1)
        ))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    void simultaneousDeathsCreateOneTokenForEachKnightsController() {
        harness.addToBattlefield(player1, new KnightOfSorrows());
        harness.addToBattlefield(player1, new KnightOfSorrows());
        harness.addToBattlefield(player2, new KnightOfSorrows());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player1, "Knight of Sorrows")).isZero();
        assertThat(countPermanents(player2, "Knight of Sorrows")).isZero();
    }

    @Test
    void sacrificeCostTriggersAfterlifeBeforeTheSpellResolves() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KnightOfSorrows());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KnightOfSorrows());
        harness.setHand(player1, List.of(new FinalPayment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstantWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        harness.assertInGraveyard(player1, "Knight of Sorrows");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Knight of Sorrows");

        harness.passBothPriorities();
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Knight of Sorrows");
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(1);
    }
}
