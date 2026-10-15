package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChariotOfVictory.class, PensiveMinotaur.class})
class ChariotOfVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has first strike, trample, and haste")
    void equippedCreatureHasKeywords() {
        Permanent chariot = addChariot(player1);
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());

        chariot.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Equipped creature loses the keywords when the Equipment is unattached")
    void keywordsLostWhenUnattached() {
        Permanent chariot = addChariot(player1);
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        chariot.setAttachedTo(creature.getId());

        chariot.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Only the equipped creature gains the keywords")
    void onlyEquippedCreatureGainsKeywords() {
        Permanent chariot = addChariot(player1);
        Permanent equipped = addCreatureReady(player1, new PensiveMinotaur());
        Permanent other = addCreatureReady(player1, new PensiveMinotaur());
        chariot.setAttachedTo(equipped.getId());

        assertThat(gqs.hasKeyword(gd, equipped, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Equip ability attaches Chariot of Victory to a creature")
    void equipAbilityAttachesToCreature() {
        Permanent chariot = addChariot(player1);
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(chariot.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reequippingTransfersAllKeywords() {
        Permanent chariot = addChariot(player1);
        Permanent original = addCreatureReady(player1, new PensiveMinotaur());
        Permanent replacement = addCreatureReady(player1, new PensiveMinotaur());
        chariot.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, replacement.getId());
        assertThat(chariot.getAttachedTo()).isEqualTo(original.getId());
        harness.passBothPriorities();

        assertThat(chariot.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        for (Keyword keyword : new Keyword[]{Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.HASTE}) {
            assertThat(gqs.hasKeyword(gd, original, keyword)).isFalse();
            assertThat(gqs.hasKeyword(gd, replacement, keyword)).isTrue();
        }
    }

    @Test
    void failedEquipLeavesOriginalAttachmentAndKeywords() {
        Permanent chariot = addChariot(player1);
        Permanent original = addCreatureReady(player1, new PensiveMinotaur());
        Permanent target = addCreatureReady(player1, new PensiveMinotaur());
        chariot.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(chariot.getAttachedTo()).isEqualTo(original.getId());
        for (Keyword keyword : new Keyword[]{Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.HASTE}) {
            assertThat(gqs.hasKeyword(gd, original, keyword)).isTrue();
        }
    }

    @Test
    void cannotEquipOpponentsCreature() {
        addChariot(player1);
        Permanent creature = addCreatureReady(player2, new PensiveMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotEquipDuringCombat() {
        addChariot(player1);
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addChariot(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ChariotOfVictory());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
