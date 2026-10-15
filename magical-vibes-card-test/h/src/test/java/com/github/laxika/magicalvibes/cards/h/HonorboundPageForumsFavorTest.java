package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HonorboundPageForumsFavor.class, GrizzlyBears.class})
class HonorboundPageForumsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Honorbound Page and exiles a castable Forum's Favor copy")
    void entersPrepared() {
        Permanent page = castHonorboundPage();

        assertThat(page.isPrepared()).isTrue();
        UUID copyId = page.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting Forum's Favor unprepares Honorbound Page and gives the target +1/+0 and flying")
    void castingForumFavorUnpreparesAndBoostsTarget() {
        Permanent page = castHonorboundPage();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = page.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(page.isPrepared()).isFalse();
        assertThat(page.getPreparedSpellCardId()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("When prepared Honorbound Page leaves the battlefield, its exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent page = castHonorboundPage();
        UUID copyId = page.getPreparedSpellCardId();

        page.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(page);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("Honorbound Page is prepared as it enters, without a triggered ability")
    void preparedImmediatelyOnEntry() {
        harness.castFromHand(player1, new HonorboundPageForumsFavor(), "{3}{W}");
        harness.passBothPriorities();

        Permanent page = findPermanent(player1, "Honorbound Page");
        assertThat(page.isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Honorbound Page also enters prepared when it is not cast")
    void preparedOnNoncastEntry() {
        Permanent page = harness.enterBattlefieldAndReturn(player1, new HonorboundPageForumsFavor());

        assertThat(page.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(page.getPreparedSpellCardId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unused prepared spell remains castable on a later turn")
    void preparationPersistsUntilUsed() {
        Permanent page = castHonorboundPage();
        UUID copyId = page.getPreparedSpellCardId();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(page.isPrepared()).isTrue();
        assertThat(page.getPreparedSpellCardId()).isEqualTo(copyId);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, copyId, page.getId());
        harness.passBothPriorities();

        assertThat(page.isPrepared()).isFalse();
        assertThat(gqs.getEffectivePower(gd, page)).isEqualTo(4);
        assertThat(page.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting the prepared spell unprepares the creature before the spell resolves")
    void unpreparedBeforeResolutionAndSpellSurvivesSourceDeath() {
        Permanent page = castHonorboundPage();
        Permanent target = addCreatureReady(player2, new HonorboundPageForumsFavor());
        UUID copyId = page.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, copyId, target.getId());

        assertThat(page.isPrepared()).isFalse();
        assertThat(page.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();

        page.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        harness.assertNotInGraveyard(player1, "Forum's Favor");
    }

    @Test
    @DisplayName("Forum's Favor boost and flying expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent page = castHonorboundPage();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, page.getPreparedSpellCardId(), page.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, page)).isEqualTo(4);
        assertThat(page.hasKeyword(Keyword.FLYING)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, page)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, page)).isEqualTo(3);
        assertThat(page.hasKeyword(Keyword.FLYING)).isFalse();
        assertThat(page.isPrepared()).isFalse();
    }

    @Test
    @DisplayName("A prepared spell still requires payment of its mana cost")
    void insufficientManaPreservesPreparation() {
        Permanent page = castHonorboundPage();
        UUID copyId = page.getPreparedSpellCardId();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, page.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(page.isPrepared()).isTrue();
        assertThat(page.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Forum's Favor cannot be cast during combat")
    void sorceryTimingPreservesPreparation() {
        Permanent page = castHonorboundPage();
        UUID copyId = page.getPreparedSpellCardId();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, page.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(page.isPrepared()).isTrue();
        assertThat(page.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Honorbound Page kills a blocker before it can deal regular combat damage")
    void firstStrikePreventsBlockerDamage() {
        Permanent page = addCreatureReady(player1, new HonorboundPageForumsFavor());
        page.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Honorbound Page");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(page.getMarkedDamage()).isZero();
    }

    private Permanent castHonorboundPage() {
        harness.castFromHand(player1, new HonorboundPageForumsFavor(), "{3}{W}");
        harness.passBothPriorities();
        resolveAllTriggers();

        return findPermanent(player1, "Honorbound Page");
    }
}
