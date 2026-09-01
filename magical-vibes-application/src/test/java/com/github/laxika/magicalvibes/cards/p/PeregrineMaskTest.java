package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeregrineMask.class, GrizzlyBears.class})
class PeregrineMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Peregrine Mask gives the creature defender, flying, and first strike")
    void equippingGrantsKeywords() {
        Permanent mask = addMaskReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addMaskReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addMaskReady(Player player) {
        Permanent mask = new Permanent(new PeregrineMask());
        mask.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mask);
        return mask;
    }
}
