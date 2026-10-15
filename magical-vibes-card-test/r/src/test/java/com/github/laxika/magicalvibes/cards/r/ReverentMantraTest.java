package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReverentMantra.class, FreshVolunteers.class, DeadlyInsect.class, Plains.class})
class ReverentMantraTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures gain protection from the chosen color")
    void grantsProtectionToAllCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, ownOtherCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingCreature, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opposingLand, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownCreature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, ownOtherCreature, CardColor.BLUE)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, opposingCreature, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Can be cast by exiling a white card instead of paying mana")
    void castsWithWhiteCardAlternateCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        harness.setHand(player1, List.of(new ReverentMantra(), new FreshVolunteers()));

        harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName()).containsExactly("Fresh Volunteers");
    }

    @Test
    @DisplayName("Alternate cost requires a white card")
    void alternateCostRequiresWhiteCard() {
        harness.setHand(player1, List.of(new ReverentMantra(), new DeadlyInsect()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DeadlyInsect());

        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection applies to creatures present on resolution, not creatures entering later")
    void snapshotsCreaturesOnResolution() {
        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        assertThat(gqs.hasProtectionFrom(gd, beforeResolution, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, afterResolution, CardColor.WHITE)).isFalse();
    }

    @Test
    @DisplayName("The caster chooses one of the five colors even with no creatures")
    void resolvesWithoutCreatures() {
        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Reverent Mantra");
    }

    @Test
    @DisplayName("A white card before the spell in hand can pay the alternate cost")
    void exilesCardBeforeSpellInHand() {
        harness.setHand(player1, List.of(new FreshVolunteers(), new ReverentMantra()));

        harness.castInstantWithAlternateExileFromHand(player1, 1, List.of(), 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName()).containsExactly("Fresh Volunteers");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.assertInGraveyard(player1, "Reverent Mantra");
    }

    @Test
    @DisplayName("The spell itself cannot pay its alternate cost")
    void cannotExileItself() {
        harness.setHand(player1, List.of(new ReverentMantra()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Plains is colorless and cannot pay the white-card alternate cost")
    void cannotExilePlains() {
        harness.setHand(player1, List.of(new ReverentMantra(), new Plains()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(player1, 0, List.of(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colorless cannot be chosen as a color")
    void rejectsColorlessChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "COLORLESS"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "GREEN");
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Artifacts cannot be chosen as a color")
    void rejectsArtifactChoice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DeadlyInsect());
        harness.castFromHand(player1, new ReverentMantra(), "{3}{W}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "ARTIFACT"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.hasProtectionFrom(gd, creature, CardColor.BLUE)).isTrue();
    }
}
