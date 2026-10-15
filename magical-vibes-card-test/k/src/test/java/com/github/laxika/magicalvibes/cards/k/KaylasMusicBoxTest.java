package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

        assertThat(gd.exilePlayPermissions)
                .containsEntry(land.getId(), player1.getId())
                .containsEntry(spell.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn)
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
}
