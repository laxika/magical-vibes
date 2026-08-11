package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnthousaSetessanHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic animates up to three target lands you control as 2/2 Warriors")
    void heroicAnimatesThreeLands() {
        harness.addToBattlefield(player1, new AnthousaSetessanHero());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID anthousaId = harness.getPermanentId(player1, "Anthousa, Setessan Hero");
        harness.castInstant(player1, 0, anthousaId);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        for (Permanent land : List.of(first, second, third)) {
            assertThat(land.isAnimatedUntilEndOfTurn()).isTrue();
            assertThat(land.getEffectivePower()).isEqualTo(2);
            assertThat(land.getEffectiveToughness()).isEqualTo(2);
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
            assertThat(land.getTransientSubtypes()).contains(CardSubtype.WARRIOR);
        }
        assertThat(opponentLand.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("Heroic can choose fewer than three lands")
    void heroicCanChooseFewerLands() {
        harness.addToBattlefield(player1, new AnthousaSetessanHero());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent otherForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID anthousaId = harness.getPermanentId(player1, "Anthousa, Setessan Hero");
        harness.castInstant(player1, 0, anthousaId);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(otherForest.isAnimatedUntilEndOfTurn()).isFalse();
    }

    @Test
    @DisplayName("Heroic does not trigger from a spell targeting a player")
    void playerTargetDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new AnthousaSetessanHero());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(forest.isAnimatedUntilEndOfTurn()).isFalse();
    }
}
