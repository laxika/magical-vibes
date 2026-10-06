package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.h.HoodedBrawler;
import com.github.laxika.magicalvibes.cards.s.StewardOfSolidarity;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VizierOfTheTrueTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert choice before any target is chosen")
    void attackOffersExertChoice() {
        addCreatureReady(player1, new VizierOfTheTrue());
        addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exerting taps the target and skips the Vizier's next untap")
    void exertTapsTargetAndSkipsUntap() {
        Permanent vizier = addCreatureReady(player1, new VizierOfTheTrue());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        resolveAllTriggers();

        assertThat(dreadmaw.isTapped()).isTrue();
        assertThat(vizier.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Declining exert taps nothing and does not skip untap")
    void decliningExertDoesNothing() {
        Permanent vizier = addCreatureReady(player1, new VizierOfTheTrue());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(dreadmaw.isTapped()).isFalse();
        assertThat(vizier.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Only creatures an opponent controls are legal targets")
    void targetFilterExcludesOwnCreatures() {
        addCreatureReady(player1, new VizierOfTheTrue());
        Permanent ownDreadmaw = addCreatureReady(player1, new ColossalDreadmaw());
        Permanent opponentDreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction interaction = gd.interaction.activeInteraction();
        assertThat(interaction).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) interaction;

        assertThat(choice.validIds()).contains(opponentDreadmaw.getId());
        assertThat(choice.validIds()).doesNotContain(ownDreadmaw.getId());
    }

    @Test
    @DisplayName("Exerting another creature as it attacks also triggers the tap")
    void exertingAnotherAttackerTriggers() {
        addCreatureReady(player1, new VizierOfTheTrue());
        addCreatureReady(player1, new HoodedBrawler());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        declareAttackers(List.of(1));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        resolveAllTriggers();

        assertThat(dreadmaw.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exerting a creature as an activation cost also triggers the tap")
    void activationCostExertTriggers() {
        addCreatureReady(player1, new VizierOfTheTrue());
        addCreatureReady(player1, new StewardOfSolidarity());
        Permanent dreadmaw = addCreatureReady(player2, new ColossalDreadmaw());

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, dreadmaw.getId());
        resolveAllTriggers();

        assertThat(dreadmaw.isTapped()).isTrue();
    }
}
