package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.t.TrustyMachete;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorDuelist.class, TrustyMachete.class})
class KorDuelistTest extends BaseCardTest {

    @Test
    void withoutEquipmentDoesNotHaveDoubleStrike() {
        Permanent duelist = addDuelistReady(player1);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void whileEquippedHasDoubleStrike() {
        Permanent duelist = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(duelist.getId());

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void losesDoubleStrikeWhenEquipmentIsDetached() {
        Permanent duelist = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(duelist.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void equipmentControlledByOpponentStillGrantsDoubleStrike() {
        Permanent duelist = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player2);
        equipment.setAttachedTo(duelist.getId());

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void retainsDoubleStrikeWhileAnotherEquipmentRemainsAttached() {
        Permanent duelist = addDuelistReady(player1);
        Permanent first = addEquipmentReady(player1);
        Permanent second = addEquipmentReady(player1);
        first.setAttachedTo(duelist.getId());
        second.setAttachedTo(duelist.getId());

        first.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, duelist, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void movingEquipmentUpdatesBothDuelistsImmediately() {
        Permanent first = addDuelistReady(player1);
        Permanent second = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();

        equipment.setAttachedTo(second.getId());

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void equippedDuelistDealsBothCombatDamageSteps() {
        Permanent duelist = addDuelistReady(player1);
        Permanent equipment = addEquipmentReady(player1);
        equipment.setAttachedTo(duelist.getId());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void unequippedDuelistDealsOnlyRegularCombatDamage() {
        addDuelistReady(player1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    private Permanent addDuelistReady(Player player) {
        return addCreatureReady(player, new KorDuelist());
    }

    private Permanent addEquipmentReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TrustyMachete());
    }
}
