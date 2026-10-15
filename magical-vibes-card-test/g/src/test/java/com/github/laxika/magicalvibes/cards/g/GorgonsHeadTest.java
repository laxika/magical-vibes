package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NyxbornTriton;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorgonsHead.class, GrizzlyBears.class, NyxbornTriton.class})
class GorgonsHeadTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature has deathtouch")
    void equippedCreatureHasDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addHeadAttached(player1, creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Unequipped creatures do not have deathtouch from Gorgon's Head")
    void unequippedCreaturesDoNotHaveDeathtouch() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addHeadReady(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Creature loses deathtouch when Gorgon's Head is removed")
    void creatureLosesDeathtouchWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent head = addHeadAttached(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(head);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Resolving equip attaches Gorgon's Head and grants deathtouch")
    void resolvingEquipAttachesAndGrantsDeathtouch() {
        Permanent head = addHeadReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(head.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void reEquippingMovesDeathtouchOnlyOnResolution() {
        Permanent head = addHeadReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        head.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(head.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isFalse();

        harness.passBothPriorities();

        assertThat(head.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void invalidReEquipTargetPreservesOriginalAttachment() {
        Permanent head = addHeadReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        head.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.passBothPriorities();

        assertThat(head.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent head = addHeadReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(head.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipDuringCombat() {
        addHeadReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equippedCreatureKillsBlockerWithLessThanLethalDamage() {
        Permanent attacker = addCreatureReady(player1, new NyxbornTriton());
        addHeadAttached(player1, attacker);
        Permanent blocker = addCreatureReady(player2, new NyxbornTriton());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player2, "Nyxborn Triton");
        harness.assertNotOnBattlefield(player2, "Nyxborn Triton");
        harness.assertOnBattlefield(player1, "Nyxborn Triton");
        harness.assertNotInGraveyard(player1, "Nyxborn Triton");
    }

    private Permanent addHeadReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GorgonsHead());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addHeadAttached(Player player, Permanent creature) {
        Permanent head = addHeadReady(player);
        head.setAttachedTo(creature.getId());
        return head;
    }
}
