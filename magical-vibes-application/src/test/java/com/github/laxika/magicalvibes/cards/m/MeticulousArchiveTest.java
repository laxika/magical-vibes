package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeticulousArchive.class, GrizzlyBears.class})
class MeticulousArchiveTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new MeticulousArchive()));

        harness.playLand(player1, 0);
        Permanent archive = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(archive.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        Permanent archive = addReadyArchive();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        Permanent archive = addReadyArchive();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(archive.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyArchive() {
        Permanent archive = new Permanent(new MeticulousArchive());
        archive.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(archive);
        return archive;
    }
}
