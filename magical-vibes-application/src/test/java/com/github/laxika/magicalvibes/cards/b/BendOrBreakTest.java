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
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .contains("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getName())
                .contains("Mountain");
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
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .contains("Forest");
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
}
