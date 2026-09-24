package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KlawMasterOfSound.class, Forest.class, GrizzlyBears.class})
class KlawMasterOfSoundTest extends BaseCardTest {

    @Test
    void gainsIndestructibleWhenCastingASpellFromExile() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, spell.getId(), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void gainsIndestructibleWhenPlayingALandFromExile() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        Forest land = new Forest();
        gd.addToExile(player1.getId(), land);
        gd.exilePlayPermissions.put(land.getId(), player1.getId());

        prepareMainPhase();
        gs.playCardFromExile(gd, player1, land.getId(), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void combatDamageExilesOpponentsTopCardFaceDownWithPlayPermission() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.ownerId()).isEqualTo(player2.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayAnyManaTypeWhileExiled).contains(topCard.getId());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
