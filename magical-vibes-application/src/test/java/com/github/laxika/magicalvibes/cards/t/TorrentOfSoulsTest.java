package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TorrentOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Only {B} spent: reanimates the graveyard creature, no boost or haste")
    void blackOnlyReanimatesNoBoost() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorrentOfSouls()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player1.getId()));
        harness.passBothPriorities();

        // Creature reanimated onto the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()));

        // No {R} spent — no boost or haste
        assertThat(existing.getEffectivePower()).isEqualTo(2);
        assertThat(existing.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only {R} spent: boosts and hastes target player's creatures, no reanimate")
    void redOnlyBoostsAndHastes() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorrentOfSouls()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();

        // No {B} spent — graveyard creature untouched
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("{B}{R} spent: reanimates and boosts/hastes target player's creatures")
    void bothColorsReanimateAndBoost() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorrentOfSouls()));
        // {4} paid with red (R spent), hybrid {B/R} paid with black (B spent).
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player1.getId()));
        harness.passBothPriorities();

        // Reanimated
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(graveyardCreature.getId()));

        // Boosted + haste
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Boost and haste wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorrentOfSouls()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("The graveyard target must be a creature card")
    void graveyardTargetMustBeACreature() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));

        harness.setHand(player1, List.of(new TorrentOfSouls()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId(), List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
