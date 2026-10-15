package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChoArrimBruiser.class, FreshVolunteers.class, Forest.class})
class ChoArrimBruiserTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the attack trigger taps two target creatures")
    void acceptingTapsTwoCreatures() {
        Permanent bruiser = addReadyBruiser();
        Permanent first = addCreatureReady(player2, new FreshVolunteers());
        Permanent second = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting the attack trigger taps one target creature")
    void acceptingTapsOneCreature() {
        Permanent bruiser = addReadyBruiser();
        Permanent target = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting the attack trigger with no targets does nothing")
    void acceptingWithoutTargetsDoesNothing() {
        Permanent bruiser = addReadyBruiser();
        Permanent target = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Accepting the attack trigger can tap a creature its controller controls")
    void acceptingCanTapOwnCreature() {
        Permanent bruiser = addReadyBruiser();
        Permanent target = addCreatureReady(player1, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves its targets untapped")
    void decliningDoesNothing() {
        Permanent bruiser = addReadyBruiser();
        Permanent target = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent bruiser = addReadyBruiser();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger cannot choose more than two creatures")
    void cannotTargetThreeCreatures() {
        Permanent bruiser = addReadyBruiser();
        Permanent first = addCreatureReady(player2, new FreshVolunteers());
        Permanent second = addCreatureReady(player2, new FreshVolunteers());
        Permanent third = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger still taps its remaining target when the other leaves")
    void tapsRemainingLegalTarget() {
        Permanent bruiser = addReadyBruiser();
        Permanent first = addCreatureReady(player2, new FreshVolunteers());
        Permanent second = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(second.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger does not resolve when its only target leaves")
    void doesNotResolveWithoutLegalTargets() {
        Permanent bruiser = addReadyBruiser();
        Permanent target = addCreatureReady(player2, new FreshVolunteers());
        Permanent unchosen = addCreatureReady(player2, new FreshVolunteers());

        declareAttackers(List.of(indexOf(player1, bruiser)));
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyBruiser() {
        return addCreatureReady(player1, new ChoArrimBruiser());
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
