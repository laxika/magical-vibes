package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BasalThrull;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({DivineSmite.class, BasalThrull.class, Forest.class, GarrukWildspeaker.class, GrizzlyBears.class})
class DivineSmiteTest extends BaseCardTest {

    @Test
    @DisplayName("Phases out a nonblack opposing creature")
    void phasesOutNonblackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castOn(target.getId());

        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Exiles a black opposing creature")
    void exilesBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BasalThrull());

        castOn(target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gd.phasedOutPermanents.getOrDefault(player2.getId(), List.of()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Can target an opposing planeswalker")
    void phasesOutOpposingPlaneswalker() {
        Permanent target = new Permanent(new GarrukWildspeaker());
        target.setCounterCount(CounterType.LOYALTY, 3);
        gd.playerBattlefields.get(player2.getId()).add(target);

        castOn(target.getId());

        assertThat(gd.phasedOutPermanents.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by the caster")
    void cannotTargetOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    private void castOn(UUID targetId) {
        prepareSpell();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new DivineSmite()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
