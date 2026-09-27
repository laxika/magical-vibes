package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({Fireshrieker.class, AlphaMyr.class})
class FireshriekerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Fireshrieker and resolving puts it on the battlefield unattached")
    void castingAndResolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Fireshrieker()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Fireshrieker")
                        && !p.isAttached());
    }

    @Test
    @DisplayName("Resolving equip attaches Fireshrieker to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature has double strike")
    void equippedCreatureHasDoubleStrike() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses double strike when Fireshrieker is removed")
    void creatureLosesDoubleStrikeWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fireshrieker);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fireshrieker does not grant double strike to unequipped creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        Permanent otherCreature = addCreatureReady(player1, new AlphaMyr());
        Permanent fireshrieker = addFireshriekerReady(player1);
        fireshrieker.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Fireshrieker can be moved to another creature")
    void canReEquipToAnotherCreature() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature1 = addCreatureReady(player1, new AlphaMyr());
        Permanent creature2 = addCreatureReady(player1, new AlphaMyr());

        fireshrieker.setAttachedTo(creature1.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gqs.hasKeyword(gd, creature1, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature2, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip fizzles if target creature is removed before resolution")
    void equipFizzlesIfTargetRemoved() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Alpha Myr"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent remaining = findPermanent(player1, "Fireshrieker");
        assertThat(remaining.getAttachedTo()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot equip during an opponent's turn")
    void cannotEquipDuringOpponentsTurn() {
        addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot equip a creature controlled by an opponent")
    void cannotEquipOpponentsCreature() {
        addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player2, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot equip a noncreature permanent")
    void cannotEquipNoncreaturePermanent() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fireshrieker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Equip pays two generic mana")
    void equipPaysTwoGenericMana() {
        Permanent fireshrieker = addFireshriekerReady(player1);
        Permanent creature = addCreatureReady(player1, new AlphaMyr());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(fireshrieker.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addFireshriekerReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Fireshrieker());
        perm.setSummoningSick(false);
        return perm;
    }
}
