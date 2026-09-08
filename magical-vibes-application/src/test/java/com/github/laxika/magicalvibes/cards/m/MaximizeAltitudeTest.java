package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class MaximizeAltitudeTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +1/+1 and flying until end of turn")
    void boostsAndGrantsFlying() {
        UUID bearsId = castOnBears();
        harness.passBothPriorities();

        Permanent bears = getBears(bearsId);
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        UUID bearsId = castOnBears();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = getBears(bearsId);
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Jump-start discards a card, applies the spell, and exiles it")
    void jumpStartDiscardsAppliesAndExiles() {
        MaximizeAltitude spell = new MaximizeAltitude();
        Plains discarded = new Plains();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castJumpStart(player1, 0, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        Permanent battlefieldBears = getBears(harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(battlefieldBears.getEffectivePower()).isEqualTo(3);
        assertThat(battlefieldBears.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.setHand(player1, List.of(new MaximizeAltitude()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.addToBattlefield(player1, new DarksteelRelic());
        UUID targetId = harness.getPermanentId(player1, "Darksteel Relic");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private UUID castOnBears() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximizeAltitude()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        return bearsId;
    }

    private Permanent getBears(UUID bearsId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(bearsId))
                .findFirst()
                .orElseThrow();
    }
}
