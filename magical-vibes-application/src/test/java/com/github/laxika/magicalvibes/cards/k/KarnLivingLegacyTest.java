package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KarnLivingLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a tapped Powerstone token")
    void plusOneCreatesTappedPowerstone() {
        Permanent karn = addReadyKarn(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent powerstone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Powerstone"))
                .findFirst()
                .orElseThrow();
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(powerstone.getCard().getSubtypes()).contains(CardSubtype.POWERSTONE);
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-1 pays X and puts one looked-at card into hand")
    void minusOnePaysAndChoosesOneCard() {
        Permanent karn = addReadyKarn(4);
        Card first = new GrizzlyBears();
        Card second = new Forest();
        Card belowLookedCards = new Ornithopter();
        harness.setLibrary(player1, List.of(first, second, belowLookedCards));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.XValueChoice xChoice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(xChoice).isNotNull();
        assertThat(xChoice.maxValue()).isEqualTo(2);
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowLookedCards, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-7 gives artifacts a tap ability that deals 1 damage")
    void minusSevenGivesArtifactsDamageAbility() {
        Permanent karn = addReadyKarn(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.setSummoningSick(false);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, artifact)).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    private Permanent addReadyKarn(int loyalty) {
        Permanent karn = new Permanent(new KarnLivingLegacy());
        karn.setCounterCount(CounterType.LOYALTY, loyalty);
        karn.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(karn);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return karn;
    }
}
