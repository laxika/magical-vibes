package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoreholdCommandTest extends BaseCardTest {

    @Test
    @DisplayName("creates a Spirit and grants own creatures power, indestructible, and haste")
    void createsSpiritAndBoostsCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castCommand();

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 1}, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Spirit"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("deals damage to any target and gives a different target player life")
    void damagesAnyTargetAndGivesTargetPlayerLife() {
        castCommand();

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 2}, null,
                List.of(player2.getId(), player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("requires the life-gain target to be a player")
    void lifeGainModeRejectsPermanentTarget() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        castCommand();

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{0, 2}, null,
                List.of(player2.getId(), permanent.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("sacrifices a permanent and then draws two cards")
    void sacrificesPermanentThenDrawsTwoCards() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new HillGiant()));
        castCommand();

        harness.castModalInstantWithModes(player1, 0, 2, new int[]{0, 3}, null, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Hill Giant");
    }

    private void castCommand() {
        harness.setHand(player1, List.of(new LoreholdCommand()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
