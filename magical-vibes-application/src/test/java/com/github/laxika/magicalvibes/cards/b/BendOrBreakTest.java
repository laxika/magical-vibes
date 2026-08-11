package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BendOrBreakTest extends BaseCardTest {

    @Test
    @DisplayName("Each player separates nontoken lands, destroys one pile, and taps the other")
    void separatesDestroysAndTapsLands() {
        Permanent player1Destroyed = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent player1Tapped = harness.addToBattlefieldAndReturn(player1, new Forest());
        Forest tokenForest = new Forest();
        tokenForest.setToken(true);
        Permanent player1TokenLand = harness.addToBattlefieldAndReturn(player1, tokenForest);

        Permanent player2Tapped = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent player2Destroyed = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new BendOrBreak()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(player1Destroyed.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Tapped.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .contains(player1Tapped.getId(), player1TokenLand.getId())
                .doesNotContain(player1Destroyed.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .contains(player2Tapped.getId())
                .doesNotContain(player2Destroyed.getId());
        assertThat(player1Tapped.isTapped()).isTrue();
        assertThat(player2Tapped.isTapped()).isTrue();
        assertThat(player1TokenLand.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .contains("Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(card -> card.getName())
                .contains("Mountain");
    }
}
