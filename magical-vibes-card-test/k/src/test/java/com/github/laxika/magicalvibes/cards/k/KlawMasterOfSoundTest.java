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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.castFromExile(player1, spell.getId());
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
        harness.castFromExile(player1, land.getId());
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

    @Test
    void canCastStolenSpellWithColorlessManaAndGainIndestructible() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        GrizzlyBears stolen = new GrizzlyBears();
        harness.setLibrary(player2, List.of(stolen));
        resolveCombatAndTrigger();

        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(stolen.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolen.getId()));
    }

    @Test
    void canPlayStolenLandAndGainIndestructible() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        Forest stolen = new Forest();
        harness.setLibrary(player2, List.of(stolen));
        resolveCombatAndTrigger();

        prepareMainPhase();
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.findExiledCard(stolen.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolen.getId()));
    }

    @Test
    void stolenCardRemainsPlayableAfterKlawLeavesBattlefield() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        GrizzlyBears stolen = new GrizzlyBears();
        harness.setLibrary(player2, List.of(stolen));
        resolveCombatAndTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(klaw);

        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, stolen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(stolen.getId()));
    }

    @Test
    void playingCardsFromHandDoesNotGrantIndestructible() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears()));
        prepareMainPhase();
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void opponentsPlayFromExileDoesNotGrantIndestructible() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player2.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castFromExile(player2, spell.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, klaw, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void stolenCreatureStillRequiresNormalCastingTiming() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        GrizzlyBears stolen = new GrizzlyBears();
        harness.setLibrary(player2, List.of(stolen));
        resolveCombatAndTrigger();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();
    }

    @Test
    void ownerCannotUseKlawsPermissionToCastTheirStolenCard() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        GrizzlyBears stolen = new GrizzlyBears();
        harness.setLibrary(player2, List.of(stolen));
        resolveCombatAndTrigger();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player2, stolen.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(stolen.getId())).isNotNull();
    }

    @Test
    void combatDamageToPlayerWithEmptyLibraryExilesNothing() {
        Permanent klaw = addCreatureReady(player1, new KlawMasterOfSound());
        klaw.setAttacking(true);
        harness.setLibrary(player2, List.of());
        int exiledBefore = gd.exiledCards.size();

        resolveCombatAndTrigger();

        assertThat(gd.exiledCards).hasSize(exiledBefore);
        harness.assertLife(player2, 17);
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
