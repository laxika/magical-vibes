package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.m.MuscleBurst;
import com.github.laxika.magicalvibes.cards.n.NantukoDisciple;
import com.github.laxika.magicalvibes.cards.s.SetonsDesire;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreetopSentinel.class, DwarvenGrunt.class, Firebolt.class, MuscleBurst.class,
        NantukoDisciple.class, SetonsDesire.class, WoodlandDruid.class})
class TreetopSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Green flying creature cannot block Treetop Sentinel")
    void greenCreatureCannotBlock() {
        Permanent attacker = addCreatureReady(player1, new TreetopSentinel());
        attacker.setAttacking(true);

        WoodlandDruid greenBird = new WoodlandDruid();
        greenBird.setKeywords(EnumSet.of(Keyword.FLYING));
        addCreatureReady(player2, greenBird);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Cannot be targeted by green instant")
    void cannotBeTargetedByGreenInstant() {
        Permanent sentinel = addCreatureReady(player2, new TreetopSentinel());

        addCreatureReady(player2, new WoodlandDruid());

        harness.setHand(player1, List.of(new MuscleBurst()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, sentinel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Cannot be targeted by an ability from a green creature")
    void cannotBeTargetedByGreenAbility() {
        addCreatureReady(player1, new NantukoDisciple());
        Permanent sentinel = addCreatureReady(player2, new TreetopSentinel());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, sentinel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Cannot be enchanted by a green Aura")
    void cannotBeEnchantedByGreenAura() {
        Permanent sentinel = addCreatureReady(player2, new TreetopSentinel());
        addCreatureReady(player2, new WoodlandDruid());

        harness.setHand(player1, List.of(new SetonsDesire()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, sentinel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from green");
    }

    @Test
    @DisplayName("Takes no combat damage from a green creature")
    void takesNoCombatDamageFromGreenCreature() {
        addCreatureReady(player1, new WoodlandDruid());
        Permanent sentinel = addCreatureReady(player2, new TreetopSentinel());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(sentinel.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Treetop Sentinel");
        harness.assertInGraveyard(player1, "Woodland Druid");
    }

    @Test
    @DisplayName("Can be targeted by red spell")
    void canBeTargetedByRedSpell() {
        Permanent sentinel = addCreatureReady(player1, new TreetopSentinel());
        Firebolt firebolt = new Firebolt();

        harness.setHand(player1, List.of(firebolt));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, sentinel.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(firebolt);
    }

    @Test
    @DisplayName("Treetop Sentinel cannot be blocked by a nonflying creature")
    void cannotBeBlockedByNonflyingCreature() {
        Permanent attacker = addCreatureReady(player1, new TreetopSentinel());
        attacker.setAttacking(true);

        addCreatureReady(player2, new DwarvenGrunt());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
}
