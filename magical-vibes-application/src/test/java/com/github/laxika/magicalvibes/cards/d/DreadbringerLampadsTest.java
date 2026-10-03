package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadbringerLampads.class, GrizzlyBears.class, GloriousAnthem.class})
class DreadbringerLampadsTest extends BaseCardTest {

    @Test
    @DisplayName("Its own entry grants target creature intimidate")
    void ownEntryGrantsIntimidate() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDreadbringerLampads(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("Another enchantment entering under your control triggers it")
    void allyEnchantmentEntryGrantsIntimidate() {
        harness.addToBattlefield(player1, new DreadbringerLampads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INTIMIDATE)).isTrue();
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new DreadbringerLampads());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Intimidate wears off at the end of the turn")
    void intimidateWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castDreadbringerLampads(player1, bears.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(bears.hasKeyword(Keyword.INTIMIDATE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INTIMIDATE)).isFalse();
    }

    @Test
    @DisplayName("Its entry cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new DreadbringerLampads()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Constellation can target its own source")
    void constellationCanTargetItself() {
        Permanent lampads = harness.addToBattlefieldAndReturn(player1, new DreadbringerLampads());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, lampads.getId());
        harness.passBothPriorities();

        assertThat(lampads.hasKeyword(Keyword.INTIMIDATE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Another enchantment's entry cannot target a noncreature")
    void allyEntryCannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new DreadbringerLampads());
        Permanent anthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, anthem.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Constellation does not grant intimidate to a target that left the battlefield")
    void targetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new DreadbringerLampads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player2.getId()).remove(bears);
        gd.playerGraveyards.get(player2.getId()).add(bears.getCard());
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.INTIMIDATE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castDreadbringerLampads(com.github.laxika.magicalvibes.model.Player player,
                                         java.util.UUID targetId) {
        harness.setHand(player, List.of(new DreadbringerLampads()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player, 0, targetId);
    }
}
