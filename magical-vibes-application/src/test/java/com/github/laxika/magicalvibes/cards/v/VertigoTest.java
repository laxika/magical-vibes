package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MesaFalcon;
import com.github.laxika.magicalvibes.cards.s.SibilantSpirit;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vertigo.class, MesaFalcon.class, SibilantSpirit.class, GrizzlyBears.class})
class VertigoTest extends BaseCardTest {

    @Test
    @DisplayName("Vertigo deals 2 damage to a flying creature, destroying a 1/1")
    void deals2DamageDestroysFlyer() {
        harness.addToBattlefield(player2, new MesaFalcon());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Mesa Falcon");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mesa Falcon");
        harness.assertInGraveyard(player2, "Mesa Falcon");
    }

    @Test
    @DisplayName("Surviving creature loses flying until end of turn")
    void survivorLosesFlyingUntilEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new SibilantSpirit());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();

        harness.castAndResolveInstant(player1, 0, elemental.getId());

        harness.assertOnBattlefield(player2, "Sibilant Spirit");
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player2, new SibilantSpirit()); // valid target so spell is playable
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with flying");
    }

    @Test
    @DisplayName("Does nothing if the target loses flying before resolution")
    void doesNothingIfTargetLosesFlyingBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SibilantSpirit());
        harness.setHand(player1, List.of(new Vertigo()));
        harness.setHand(player2, List.of(new Vertigo()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sibilant Spirit");
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }
}
