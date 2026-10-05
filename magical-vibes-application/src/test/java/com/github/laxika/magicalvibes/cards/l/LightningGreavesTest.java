package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.s.SkyhunterPatrol;
import com.github.laxika.magicalvibes.cards.s.SpikeshotGoblin;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
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


    @Test
    @DisplayName("Moving Greaves transfers shroud and only the newly equipped creature can attack with haste")
    void movingGreavesTransfersKeywordsWithoutRemovingSummoningSickness() {
        Permanent greaves = addGreavesReady(player1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SkyhunterPatrol());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SkyhunterPatrol());
        first.setSummoningSick(true);
        second.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(als.canAttack(gd, first, player1.getId())).isTrue();
        assertThat(als.canAttack(gd, second, player1.getId())).isFalse();

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(greaves.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.SHROUD)).isTrue();
        assertThat(als.canAttack(gd, first, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, second, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Haste permits a summoning-sick equipped creature to activate its tap ability")
    void hastePermitsTapAbility() {
        addGreavesReady(player1);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new SpikeshotGoblin());
        goblin.setSummoningSick(true);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, goblin.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(goblin.isTapped()).isTrue();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Shroud prevents equipping the already equipped creature again")
    void cannotEquipAlreadyShroudedCreature() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        greaves.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");

        assertThat(greaves.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player2, new SkyhunterPatrol());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(greaves.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void equipCannotTargetNoncreature() {
        Permanent greaves = addGreavesReady(player1);
        Permanent other = addGreavesReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(greaves.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated outside a main phase")
    void equipRequiresSorceryTiming() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(greaves.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Shroud also prevents an opponent's spell from targeting the equipped creature")
    void shroudPreventsOpponentsSpellTargeting() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        greaves.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("A spell fails to affect its target if the creature gains shroud before resolution")
    void shroudIsCheckedAgainAtResolution() {
        Permanent greaves = addGreavesReady(player1);
        Permanent creature = addCreatureReady(player1, new SkyhunterPatrol());
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, creature.getId());

        greaves.setAttachedTo(creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, greaves);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Terror");
    }

    private Permanent addGreavesReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LightningGreaves());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
