package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.h.HavengulLich;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoraciousTyphon.class, NyxbornColossus.class, HavengulLich.class})
class VoraciousTyphonTest extends BaseCardTest {

    @Test
    void castingFromHandEntersWithoutCounters() {
        harness.setHand(player1, List.of(new VoraciousTyphon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent typhon = findPermanent(player1, "Voracious Typhon");
        assertThat(typhon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapingExilesFourOtherCardsAndEntersWithThreeCounters() {
        VoraciousTyphon typhon = new VoraciousTyphon();
        NyxbornColossus first = new NyxbornColossus();
        NyxbornColossus second = new NyxbornColossus();
        NyxbornColossus third = new NyxbornColossus();
        NyxbornColossus fourth = new NyxbornColossus();
        harness.setGraveyard(player1, List.of(typhon, first, second, third, fourth));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);

        harness.passBothPriorities();

        Permanent escapedTyphon = findPermanent(player1, "Voracious Typhon");
        assertThat(escapedTyphon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(escapedTyphon.getEffectivePower()).isEqualTo(7);
        assertThat(escapedTyphon.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void escapeRequiresFourOtherCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(
                new VoraciousTyphon(), new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapingMarksThePermanentAsEscaped() {
        harness.setGraveyard(player1, List.of(new VoraciousTyphon(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Voracious Typhon").isEscaped()).isTrue();
    }

    @Test
    void castingWithHavengulLichPermissionDoesNotEscapeOrReceiveCounters() {
        harness.addToBattlefield(player1, new HavengulLich());
        VoraciousTyphon typhon = new VoraciousTyphon();
        harness.setGraveyard(player1, List.of(typhon));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, typhon.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromGraveyard(player1, typhon.getId());
        resolveAllTriggers();

        Permanent permanent = findPermanent(player1, "Voracious Typhon");
        assertThat(permanent.isEscaped()).isFalse();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapeCannotExileTheCardBeingCast() {
        harness.setGraveyard(player1, List.of(new VoraciousTyphon(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void escapeCannotExileTheSameCardMoreThanOnce() {
        harness.setGraveyard(player1, List.of(new VoraciousTyphon(),
                new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus(), new NyxbornColossus()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
