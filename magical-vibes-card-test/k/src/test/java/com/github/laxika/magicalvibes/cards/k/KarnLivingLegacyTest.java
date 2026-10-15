package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AutomaticLibrarian;
import com.github.laxika.magicalvibes.cards.m.MoltenMonstrosity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarnLivingLegacy.class, Forest.class, MoltenMonstrosity.class, AutomaticLibrarian.class})
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
        Card first = new MoltenMonstrosity();
        Card second = new Forest();
        Card belowLookedCards = new AutomaticLibrarian();
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
    @DisplayName("-7 gives an emblem that taps an artifact to deal 1 damage")
    void minusSevenEmblemDealsDamage() {
        Permanent karn = addReadyKarn(7);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());
        artifact.setSummoningSick(false);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        harness.activateEmblemAbility(player1, 0, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("The emblem can tap an artifact creature with summoning sickness")
    void emblemCanTapNewArtifactCreature() {
        addReadyKarn(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new AutomaticLibrarian());
        artifact.setSummoningSick(true);

        harness.activateEmblemAbility(player1, 0, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("-1 can pay zero without looking at or moving any cards")
    void minusOneCanPayZero() {
        addReadyKarn(4);
        Card first = new Forest();
        harness.setLibrary(player1, List.of(first));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-1 pays the full amount even when fewer cards remain in the library")
    void minusOneWithShortLibrary() {
        addReadyKarn(4);
        Card first = new Forest();
        harness.setLibrary(player1, List.of(first));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("-1 resolves without drawing when the library is empty")
    void minusOneWithEmptyLibrary() {
        addReadyKarn(4);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyKarn(int loyalty) {
        Permanent karn = harness.addToBattlefieldAndReturn(player1, new KarnLivingLegacy());
        karn.setCounterCount(CounterType.LOYALTY, loyalty);
        karn.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return karn;
    }
}
