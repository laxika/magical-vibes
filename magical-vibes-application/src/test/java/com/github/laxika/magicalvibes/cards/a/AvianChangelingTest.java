package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.w.WizenedCenn;
import com.github.laxika.magicalvibes.cards.s.SurgeOfThoughtweft;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvianChangeling.class, WizenedCenn.class, SurgeOfThoughtweft.class})
class AvianChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Wizened Cenn (Kithkin lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
    }

    @Test
    @DisplayName("Avian Changeling cannot be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        Permanent attacker = addCreatureReady(player1, new AvianChangeling());
        attacker.setAttacking(true);

        addCreatureReady(player2, new WizenedCenn());

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block")
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Avian Changeling can be blocked by another flying creature")
    void canBeBlockedByFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new AvianChangeling());
        attacker.setAttacking(true);
        addCreatureReady(player2, new AvianChangeling());
        prepareDeclareBlockers(player1);

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Avian Changeling satisfies Surge of Thoughtweft's Kithkin condition")
    void changelingEnablesKithkinCardDraw() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AvianChangeling());
        AvianChangeling drawnCard = new AvianChangeling();
        harness.setLibrary(player1, List.of(drawnCard));

        harness.castFromHand(player1, new SurgeOfThoughtweft(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
    }
}
