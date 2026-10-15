package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ErtaisTrickery;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Anavolver.class, ErtaisTrickery.class})
class AnavolverTest extends BaseCardTest {

    @Test
    @DisplayName("Without kickers, Anavolver enters without counters, flying, or regeneration")
    void withoutKickers() {
        Permanent anavolver = castAnavolver();

        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isFalse();
        assertThat(anavolver.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Blue kicker adds two counters and flying")
    void blueKicker() {
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Anavolver()));

        castWithBlueKicker();
        harness.passBothPriorities();

        Permanent anavolver = findAnavolver();
        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isTrue();
        assertThat(anavolver.getRegenerationShield()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Black kicker adds one counter and the regeneration ability")
    void blackKicker() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Anavolver()));

        castWithBlackKicker();
        harness.passBothPriorities();

        Permanent anavolver = findAnavolver();
        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isFalse();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(anavolver.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Both kickers add three counters, flying, and regeneration")
    void bothKickers() {
        addBaseMana();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Anavolver()));

        castWithBothKickers();
        harness.passBothPriorities();

        Permanent anavolver = findAnavolver();
        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isTrue();

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(anavolver.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering without being cast grants neither kicker benefit")
    void enteringWithoutCastingHasNoKickerBenefits() {
        Permanent anavolver = harness.enterBattlefieldAndReturn(player1, new Anavolver());

        assertThat(anavolver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, anavolver, Keyword.FLYING)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Paying only the black kicker makes Anavolver a legal target for Ertai's Trickery")
    void blackKickerCanBeCounteredByTrickery() {
        Anavolver card = new Anavolver();
        harness.setHand(player1, List.of(card));
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        castWithBlackKicker();
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, card.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Anavolver");
        assertThat(countPermanents(player1, "Anavolver")).isZero();
    }

    @Test
    @DisplayName("Without the black kicker, Anavolver cannot activate regeneration")
    void regenerationUnavailableWithoutBlackKicker() {
        castAnavolver();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Regeneration pays life immediately and creates its shield only on resolution")
    void regenerationUsesStackAndCanBeActivatedRepeatedly() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Anavolver()));
        castWithBlackKicker();
        harness.passBothPriorities();
        Permanent anavolver = findAnavolver();
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(anavolver.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        assertThat(anavolver.getRegenerationShield()).isEqualTo(1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        harness.passBothPriorities();
        assertThat(anavolver.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration cannot be activated with fewer than three life")
    void regenerationCannotPayMoreLifeThanAvailable() {
        addBaseMana();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player1, List.of(new Anavolver()));
        castWithBlackKicker();
        harness.passBothPriorities();
        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        assertThat(findAnavolver().getRegenerationShield()).isZero();
    }

    private Permanent castAnavolver() {
        harness.castFromHand(player1, new Anavolver(), "{3}{G}");
        harness.passBothPriorities();
        return findAnavolver();
    }

    private void castWithBlueKicker() {
        harness.castKickedCreature(player1, 0);
    }

    private void castWithBlackKicker() {
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{B}"));
    }

    private void castWithBothKickers() {
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, true, null, null, null, null,
                List.of("{B}"), false);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent findAnavolver() {
        return findPermanent(player1, "Anavolver");
    }
}
