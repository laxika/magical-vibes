package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PeregrineMask.class, Watchwolf.class})
class PeregrineMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new PeregrineMask());
        Permanent creature = addCreatureReady(player2, new Watchwolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mask.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("All granted abilities move only when the equip ability resolves")
    void keywordsMoveOnlyOnResolution() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new PeregrineMask());
        Permanent firstCreature = addCreatureReady(player1, new Watchwolf());
        Permanent secondCreature = addCreatureReady(player1, new Watchwolf());
        mask.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());

        assertThat(mask.getAttachedTo()).isEqualTo(firstCreature.getId());
        for (Keyword keyword : new Keyword[]{Keyword.DEFENDER, Keyword.FLYING, Keyword.FIRST_STRIKE}) {
            assertThat(gqs.hasKeyword(gd, firstCreature, keyword)).isTrue();
            assertThat(gqs.hasKeyword(gd, secondCreature, keyword)).isFalse();
        }

        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(secondCreature.getId());
        for (Keyword keyword : new Keyword[]{Keyword.DEFENDER, Keyword.FLYING, Keyword.FIRST_STRIKE}) {
            assertThat(gqs.hasKeyword(gd, firstCreature, keyword)).isFalse();
            assertThat(gqs.hasKeyword(gd, secondCreature, keyword)).isTrue();
        }
    }

    @Test
    @DisplayName("Equipping Peregrine Mask gives the creature defender, flying, and first strike")
    void equippingGrantsKeywords() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new PeregrineMask());
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Peregrine Mask does not grant keywords while unattached")
    void unattachedMaskDoesNotGrantKeywords() {
        Permanent creature = addCreatureReady(player1, new Watchwolf());
        harness.addToBattlefield(player1, new PeregrineMask());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Re-equipping Peregrine Mask removes its keywords from the previous creature")
    void reEquippingRemovesKeywordsFromPreviousCreature() {
        Permanent mask = harness.addToBattlefieldAndReturn(player1, new PeregrineMask());
        Permanent firstCreature = addCreatureReady(player1, new Watchwolf());
        Permanent secondCreature = addCreatureReady(player1, new Watchwolf());
        mask.setAttachedTo(firstCreature.getId());

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DEFENDER)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(mask.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.DEFENDER)).isTrue();
    }
}
