package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.h.Hystrodon;
import com.github.laxika.magicalvibes.cards.w.WretchedAnurid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoverOfDarkness.class, ElvishWarrior.class, Hystrodon.class, WretchedAnurid.class})
class CoverOfDarknessTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type gives matching creatures fear")
    void grantsFearToCreaturesOfChosenType() {
        Permanent ownElf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        Permanent ownBeast = harness.addToBattlefieldAndReturn(player1, new Hystrodon());
        Permanent opponentElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());

        castCoverChoosingElf();

        assertThat(gqs.hasKeyword(gd, ownElf, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBeast, Keyword.FEAR)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.FEAR)).isTrue();
    }

    @Test
    @DisplayName("The chosen creature type also applies to creatures entering later")
    void grantsFearToMatchingCreaturesEnteringLater() {
        castCoverChoosingElf();

        Permanent laterElf = harness.addToBattlefieldAndReturn(player2, new ElvishWarrior());
        Permanent laterBeast = harness.addToBattlefieldAndReturn(player2, new Hystrodon());

        assertThat(gqs.hasKeyword(gd, laterElf, Keyword.FEAR)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterBeast, Keyword.FEAR)).isFalse();
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking a matching creature")
    void fearPreventsNonblackNonartifactBlocker() {
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        Permanent blocker = addCreatureReady(player2, new Hystrodon());
        castCoverChoosingElf();
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Fear allows a black creature to block a matching creature")
    void fearAllowsBlackBlocker() {
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        Permanent blocker = addCreatureReady(player2, new WretchedAnurid());
        castCoverChoosingElf();
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castCoverChoosingElf() {
        harness.setHand(player1, List.of(new CoverOfDarkness()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, CardSubtype.ELF.name());
    }
}
