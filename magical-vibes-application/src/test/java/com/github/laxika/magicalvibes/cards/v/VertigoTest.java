package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VertigoTest extends BaseCardTest {

    @Test
    @DisplayName("Vertigo deals 2 damage to a flying creature, destroying a 1/1")
    void deals2DamageDestroysFlyer() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Suntail Hawk");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertInGraveyard(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Surviving creature loses flying until end of turn")
    void survivorLosesFlyingUntilEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

        harness.castInstant(player1, 0, elemental.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player2, new AirElemental()); // valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }
}
