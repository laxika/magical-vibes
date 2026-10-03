package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AliriosEnraptured.class, SternDismissal.class})
class AliriosEnrapturedTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and creates a 3/2 blue Reflection token")
    void entersTappedAndCreatesReflection() {
        Permanent alirios = castAlirios(player1);
        assertThat(alirios.isTapped()).isTrue();

        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
        Permanent reflection = findPermanent(player1, "Reflection");
        assertThat(reflection.getCard().isToken()).isTrue();
        assertThat(reflection.getEffectivePower()).isEqualTo(3);
        assertThat(reflection.getEffectiveToughness()).isEqualTo(2);
        assertThat(reflection.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(reflection.getCard().getSubtypes()).contains(CardSubtype.REFLECTION);
    }

    @Test
    @DisplayName("Does not untap while its controller controls a Reflection")
    void doesNotUntapWithReflection() {
        Permanent alirios = castAlirios(player1);

        harness.performUntapStep(player1);

        assertThat(alirios.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps normally without a Reflection under its controller's control")
    void untapsWithoutReflection() {
        Permanent alirios = addCreatureReady(player1, new AliriosEnraptured());
        alirios.tap();

        harness.performUntapStep(player1);

        assertThat(alirios.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Reflection does not prevent Alirios from untapping")
    void opponentReflectionDoesNotCount() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castAlirios(player2);
        assertThat(countPermanents(player2, "Reflection")).isEqualTo(1);

        Permanent alirios = addCreatureReady(player1, new AliriosEnraptured());
        alirios.tap();

        harness.performUntapStep(player1);

        assertThat(alirios.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Untaps after the Reflection leaves the battlefield")
    void untapsAfterReflectionLeaves() {
        Permanent alirios = castAlirios(player1);
        Permanent reflection = findPermanent(player1, "Reflection");
        harness.performUntapStep(player1);
        assertThat(alirios.isTapped()).isTrue();

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, reflection.getId());
        assertThat(countPermanents(player1, "Reflection")).isZero();

        harness.performUntapStep(player1);
        assertThat(alirios.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The Reflection trigger resolves even if Alirios leaves first")
    void createsReflectionAfterAliriosLeaves() {
        harness.setHand(player1, List.of(new AliriosEnraptured()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent alirios = findPermanent(player1, "Alirios, Enraptured");
        assertThat(alirios.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Reflection")).isZero();

        harness.setHand(player2, List.of(new SternDismissal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, alirios.getId());
        assertThat(countPermanents(player1, "Alirios, Enraptured")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Reflection")).isEqualTo(1);
    }

    private Permanent castAlirios(Player player) {
        harness.setHand(player, List.of(new AliriosEnraptured()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player, "Alirios, Enraptured");
    }

}
