package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({KitesailLarcenist.class, LlanowarElves.class, GrizzlyBears.class, DayOfJudgment.class})
class KitesailLarcenistTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms up to one artifact or creature per player and grants the Treasure ability")
    void transformsOnePermanentPerPlayerAndGrantsTreasureAbility() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castLarcenist(List.of(ownCreature.getId(), opposingCreature.getId()));

        assertTreasure(ownCreature);
        assertTreasure(opposingCreature);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Allows at most one chosen permanent per player")
    void allowsAtMostOnePermanentPerPlayer() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareLarcenistCast();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("The transformation ends when Kitesail Larcenist leaves the battlefield")
    void transformationEndsWhenSourceLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castLarcenist(List.of(target.getId()));
        assertTreasure(target);

        harness.setHand(player1, List.of(new DayOfJudgment()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kitesail Larcenist");
        assertThat(target.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(target.getCard().hasType(CardType.ARTIFACT)).isFalse();
    }

    private void castLarcenist(List<UUID> targetIds) {
        prepareLarcenistCast();
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void prepareLarcenistCast() {
        harness.setHand(player1, List.of(new KitesailLarcenist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void assertTreasure(Permanent permanent) {
        assertThat(gqs.isArtifact(gd, permanent)).isTrue();
        assertThat(gqs.isCreature(gd, permanent)).isFalse();
        assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }
}
