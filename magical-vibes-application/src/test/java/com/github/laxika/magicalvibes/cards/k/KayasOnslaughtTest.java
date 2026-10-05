package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.r.ReplicatingRing;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayasOnslaught.class, BeskirShieldmate.class, ReplicatingRing.class})
class KayasOnslaughtTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +1/+1 and double strike")
    void resolvesAllEffects() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        harness.setHand(player1, List.of(new KayasOnslaught()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        harness.setHand(player1, List.of(new KayasOnslaught()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new ReplicatingRing());
        harness.setHand(player1, List.of(new KayasOnslaught()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can be foretold and cast for white mana on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        KayasOnslaught onslaught = new KayasOnslaught();
        harness.setHand(player1, List.of(onslaught));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(onslaught.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, onslaught.getId(), bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Can boost an opponent's creature without affecting other creatures")
    void canTargetOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BeskirShieldmate());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        harness.setHand(player1, List.of(new KayasOnslaught()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(other.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Kaya's Onslaught");
    }

    @Test
    @DisplayName("Cannot cast a foretold card during the turn it was foretold")
    void cannotCastOnForetellTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BeskirShieldmate());
        KayasOnslaught onslaught = new KayasOnslaught();
        harness.setHand(player1, List.of(onslaught));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, onslaught.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(onslaught.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot foretell during an opponent's turn")
    void cannotForetellDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new KayasOnslaught()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Foretell can only be used during your turn");
        harness.assertInHand(player1, "Kaya's Onslaught");
    }
}
