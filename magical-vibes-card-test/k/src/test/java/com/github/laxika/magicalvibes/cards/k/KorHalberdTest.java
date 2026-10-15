package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostSanctifier;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorHalberd.class, AlabasterHostSanctifier.class})
class KorHalberdTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and vigilance")
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        halberd.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures do not get Kor Halberd's bonuses")
    void unequippedCreatureIsUnaffected() {
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.addToBattlefieldAndReturn(player1, new KorHalberd());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Equip {1} attaches Kor Halberd to a creature you control")
    void equipAttachesToCreature() {
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reequippingMovesAllBonusesToTheNewCreature() {
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        Permanent first = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent second = addCreatureReady(player1, new AlabasterHostSanctifier());
        halberd.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, second.getId());
        assertThat(halberd.getAttachedTo()).isEqualTo(first.getId());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void illegalEquipTargetLeavesManaAndAttachmentUnchanged() {
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        Permanent opponent = addCreatureReady(player2, new AlabasterHostSanctifier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(halberd.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void cannotEquipDuringCombat() {
        harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        Permanent creature = addCreatureReady(player1, new AlabasterHostSanctifier());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    void failedReequipKeepsOriginalAttachment() {
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        Permanent first = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent second = addCreatureReady(player1, new AlabasterHostSanctifier());
        halberd.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(halberd.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equippedAttackerDoesNotTapButUnequippedAttackerDoes() {
        Permanent equipped = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent unequipped = addCreatureReady(player1, new AlabasterHostSanctifier());
        Permanent halberd = harness.addToBattlefieldAndReturn(player1, new KorHalberd());
        halberd.setAttachedTo(equipped.getId());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(equipped.isTapped()).isFalse();
        assertThat(unequipped.isTapped()).isTrue();
    }
}