package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SkyhunterPatrol;
import com.github.laxika.magicalvibes.cards.s.SpikeshotGoblin;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningGreaves.class, SkyhunterPatrol.class, Terror.class, SpikeshotGoblin.class})
class LightningGreavesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has haste and shroud")
    void equippedCreatureHasHasteAndShroud() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();

        greaves.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creature loses haste and shroud when Greaves are unattached")
    void creatureLosesKeywordsWhenGreavesAreUnattached() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        greaves.setAttachedTo(creature.getId());

        greaves.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Zero-cost equip attaches Greaves to a creature")
    void zeroCostEquipAttachesGreaves() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(greaves.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature cannot be targeted by spells")
    void shroudPreventsSpellTargeting() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        greaves.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Terror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Equipped creature cannot be targeted by activated abilities")
    void shroudPreventsAbilityTargeting() {
        addCreatureReady(player1, new SpikeshotGoblin());
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        greaves.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private Permanent addGreavesReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LightningGreaves());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
