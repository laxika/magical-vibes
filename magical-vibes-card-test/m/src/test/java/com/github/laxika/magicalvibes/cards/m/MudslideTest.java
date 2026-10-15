package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mudslide.class, Forest.class, KjeldoranSkyknight.class, KjeldoranWarrior.class})
class MudslideTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped non-flying creature stays tapped through the untap step while a flier untaps")
    void nonFlyingCreatureStaysTappedWhileFlierUntaps() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent warrior = addTapped(player1, new KjeldoranWarrior());       // no flying
        Permanent skyknight = addTapped(player1, new KjeldoranSkyknight());   // flying

        harness.performUntapStep(player1);

        assertThat(warrior.isTapped()).isTrue();
        assertThat(skyknight.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A noncreature permanent without flying untaps normally")
    void noncreaturePermanentUntapsNormally() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent forest = addTapped(player1, new Forest());

        harness.performUntapStep(player1);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {2} untaps the chosen non-flying creature")
    void payingTwoUntapsChosenCreature() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent warrior = addTapped(player1, new KjeldoranWarrior());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(warrior.getId()));

        assertThat(warrior.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Paying {4} untaps two chosen non-flying creatures")
    void payingFourUntapsTwoCreatures() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent warriorA = addTapped(player1, new KjeldoranWarrior());
        Permanent warriorB = addTapped(player1, new KjeldoranWarrior());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(warriorA.getId(), warriorB.getId()));

        assertThat(warriorA.isTapped()).isFalse();
        assertThat(warriorB.isTapped()).isFalse();
    }

    @Test
    @DisplayName("With only {1} available, no creature can be untapped (cost of {2} not met)")
    void insufficientManaLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent warrior = addTapped(player1, new KjeldoranWarrior());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped flier is not offered by the upkeep trigger")
    void tappedFlierIsNotOfferedByUpkeepTrigger() {
        harness.addToBattlefield(player1, new Mudslide());
        advanceToUpkeep(player1);
        Permanent skyknight = addTapped(player1, new KjeldoranSkyknight());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(skyknight.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("During an opponent's upkeep, that opponent chooses only their own tapped non-fliers")
    void opponentChoosesFromTheirOwnTappedNonFliers() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent ownWarrior = addTapped(player1, new KjeldoranWarrior());
        Permanent opponentWarrior = addTapped(player2, new KjeldoranWarrior());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(opponentWarrior.getId()).doesNotContain(ownWarrior.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentWarrior.getId()));

        assertThat(opponentWarrior.isTapped()).isFalse();
        assertThat(ownWarrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Choosing no creatures leaves the non-flying creature tapped")
    void choosingNoneLeavesCreatureTapped() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent warrior = addTapped(player1, new KjeldoranWarrior());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(warrior.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapped lands can fund the optional payment during upkeep resolution")
    void offersPaymentWhenManaCanBeProducedDuringResolution() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent warrior = addTapped(player1, new KjeldoranWarrior());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(warrior.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Only chosen tapped non-fliers untap and each chosen creature costs two mana")
    void choosingSubsetPaysOnlyForChosenCreature() {
        harness.addToBattlefield(player1, new Mudslide());
        Permanent chosen = addTapped(player1, new KjeldoranWarrior());
        Permanent unchosen = addTapped(player1, new KjeldoranWarrior());
        advanceToUpkeep(player1);
        Permanent untapped = harness.addToBattlefieldAndReturn(player1, new KjeldoranWarrior());
        Permanent flier = addTapped(player1, new KjeldoranSkyknight());
        Permanent forest = addTapped(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(chosen.getId(), unchosen.getId());

        harness.withAutoStop(TurnStep.UPKEEP,
                () -> harness.handleMultiplePermanentsChosen(player1, List.of(chosen.getId())));

        assertThat(chosen.isTapped()).isFalse();
        assertThat(unchosen.isTapped()).isTrue();
        assertThat(untapped.isTapped()).isFalse();
        assertThat(flier.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    private Permanent addTapped(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }

}
