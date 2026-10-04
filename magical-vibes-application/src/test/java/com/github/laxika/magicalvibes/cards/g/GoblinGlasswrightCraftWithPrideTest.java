package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({GoblinGlasswrightCraftWithPride.class})
class GoblinGlasswrightCraftWithPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Goblin Glasswright and exiles a castable Craft with Pride copy")
    void entersPrepared() {
        Permanent glasswright = castGoblinGlasswright();

        assertThat(glasswright.isPrepared()).isTrue();
        UUID copyId = glasswright.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting the prepared Craft with Pride copy unprepares Goblin Glasswright and creates a Treasure")
    void castingPrepareCopyUnpreparesAndCreatesTreasure() {
        Permanent glasswright = castGoblinGlasswright();
        UUID copyId = glasswright.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(glasswright.isPrepared()).isFalse();
        assertThat(glasswright.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Treasure")
                        && permanent.getCard().getType() == CardType.ARTIFACT
                        && permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE));
    }

    @Test
    @DisplayName("When prepared Goblin Glasswright leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent glasswright = castGoblinGlasswright();
        UUID copyId = glasswright.getPreparedSpellCardId();

        glasswright.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(glasswright);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castGoblinGlasswright() {
        harness.castFromHand(player1, new GoblinGlasswrightCraftWithPride(), "{1}{R}");
        resolveAllTriggers();

        return findPermanent(player1, "Goblin Glasswright");
    }

    @Test
    void preparationDoesNotUseTheStack() {
        harness.castFromHand(player1, new GoblinGlasswrightCraftWithPride(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Goblin Glasswright").isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingCopyUnpreparesBeforeResolution() {
        Permanent glasswright = castGoblinGlasswright();
        UUID copyId = glasswright.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, copyId);

        assertThat(glasswright.isPrepared()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(copyId));
    }

    @Test
    void preparedCopyRequiresRedMana() {
        Permanent glasswright = castGoblinGlasswright();
        UUID copyId = glasswright.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glasswright.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void preparedSorceryCannotBeCastInEndStep() {
        Permanent glasswright = castGoblinGlasswright();
        UUID copyId = glasswright.getPreparedSpellCardId();
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(glasswright.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }
}
