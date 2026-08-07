package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CelestialFlare;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KytheonsTactics;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwiftReckoningTest extends BaseCardTest {

    private Permanent addTappedBear(com.github.laxika.magicalvibes.model.Player owner) {
        Permanent bear = new Permanent(new GrizzlyBears());
        bear.tap();
        gd.playerBattlefields.get(owner.getId()).add(bear);
        return bear;
    }

    @Test
    @DisplayName("Destroys the targeted tapped creature")
    void destroysTappedCreature() {
        Permanent bear = addTappedBear(player2);
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(c -> c.getName()).contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        addTappedBear(player1);
        Permanent untapped = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(untapped);

        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell mastery lets it be cast at instant speed")
    void spellMasteryGrantsFlashTiming() {
        Permanent bear = addTappedBear(player2);
        harness.setGraveyard(player1, List.of(new CelestialFlare(), new KytheonsTactics()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Without two instants or sorceries in the graveyard it keeps sorcery timing")
    void noFlashTimingWithoutSpellMastery() {
        Permanent bear = addTappedBear(player2);
        harness.setGraveyard(player1, List.of(new CelestialFlare()));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SwiftReckoning()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
