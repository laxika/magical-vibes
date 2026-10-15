package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BendOrBreak.class, AncientKavu.class, Forest.class, Mountain.class})
class BendOrBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Each player separates nontoken lands, destroys one pile, and taps the other")
    void separatesDestroysAndTapsLands() {
        Permanent player1Destroyed = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Tapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest tokenForest = new Forest();
        tokenForest.setToken(true);
        Permanent player1TokenLand = harness.addToBattlefieldAndReturn(player1, tokenForest);
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new AncientKavu());

        Permanent player2Tapped = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent player2Destroyed = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new BendOrBreak(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Destroyed.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Tapped.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(player1Tapped.getId(), player1TokenLand.getId(), player1Creature.getId())
                .doesNotContain(player1Destroyed.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(player2Tapped.getId())
                .doesNotContain(player2Destroyed.getId());
        assertThat(player1Tapped.isTapped()).isTrue();
        assertThat(player2Tapped.isTapped()).isTrue();
        assertThat(player1TokenLand.isTapped()).isFalse();
        assertThat(player1Creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("An empty pile is legal and nonland permanents are not included")
    void allowsEmptyPileAndIgnoresNonlandPermanents() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AncientKavu());

        harness.castFromHand(player1, new BendOrBreak(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(creature.getId())
                .doesNotContain(forest.getId());
        assertThat(creature.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Resolves without an interaction when no nontoken lands exist")
    void resolvesWithoutNontokenLands() {
        harness.castFromHand(player1, new BendOrBreak(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing an empty pile preserves and taps all lands in the other pile")
    void choosingEmptyPilePreservesAndTapsLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.castFromHand(player1, new BendOrBreak(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactlyInAnyOrder(forest.getId(), mountain.getId());
        assertThat(forest.isTapped()).isTrue();
        assertThat(mountain.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("All pile choices finish before any lands are destroyed or tapped")
    void waitsForEveryPileChoiceBeforeChangingLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new BendOrBreak(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        assertThat(forest.isTapped()).isFalse();
        assertThat(mountain.isTapped()).isFalse();
        harness.assertNotInGraveyard(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Mountain");

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertNotInGraveyard(player2, "Mountain");
        assertThat(mountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent lands are separated even when the caster controls only token lands")
    void separatesOpponentLandsAndExcludesCasterTokenLands() {
        Forest tokenForest = new Forest();
        tokenForest.setToken(true);
        Permanent tokenLand = harness.addToBattlefieldAndReturn(player1, tokenForest);
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new BendOrBreak(), "{3}{R}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(mountain.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactly(tokenLand.getId());
        assertThat(tokenLand.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }
}
