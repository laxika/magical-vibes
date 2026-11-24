package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaylasMusicBox.class, Forest.class, GrizzlyBears.class})
class KaylasMusicBoxTest extends BaseCardTest {

    @Test
    void whiteAbilityExilesTopCardFaceDownWithTheMusicBox() {
        Permanent musicBox = harness.addToBattlefieldAndReturn(player1, new KaylasMusicBox());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.sourcePermanentId()).isEqualTo(musicBox.getId());
    }

    @Test
    void tapAbilityGrantsEndOfTurnPermissionForTrackedLandAndSpell() {
        Permanent musicBox = harness.addToBattlefieldAndReturn(player1, new KaylasMusicBox());
        Forest land = new Forest();
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), land, musicBox.getId());
        gd.addToExile(player1.getId(), spell, musicBox.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissionHolders())
                .containsEntry(land.getId(), player1.getId())
                .containsEntry(spell.getId(), player1.getId());
        assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN))
                .contains(land.getId(), spell.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, land.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The tap ability lets you play only the exiled cards you own")
    void tapAbilityCoversOnlyCardsYouOwn() {
        Permanent musicBox = harness.addToBattlefieldAndReturn(player1, new KaylasMusicBox());
        GrizzlyBears own = new GrizzlyBears();
        GrizzlyBears opponents = new GrizzlyBears();
        gd.addToExile(player1.getId(), own, musicBox.getId(), true);
        gd.addToExile(player2.getId(), opponents, musicBox.getId(), true);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.exilePlayPermissionHolders())
                .containsEntry(own.getId(), player1.getId())
                .doesNotContainKey(opponents.getId());
    }

    @Test
    @DisplayName("A card exiled with the music box later that turn may be played too")
    void cardExiledLaterThisTurnIsCovered() {
        Permanent musicBox = harness.addToBattlefieldAndReturn(player1, new KaylasMusicBox());
        GrizzlyBears later = new GrizzlyBears();
        harness.setLibrary(player1, List.of(later));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        musicBox.untap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(later.getId()).sourcePermanentId()).isEqualTo(musicBox.getId());
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(later.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, later.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
