package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MeticulousExcavation;
import com.github.laxika.magicalvibes.cards.w.WingCommando;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustGoliath.class, MeticulousExcavation.class, WingCommando.class})
class RustGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Prototype cast uses the alternate characteristics and keeps reach and trample")
    void prototypeCastUsesAlternateCharacteristics() {
        harness.setHand(player1, List.of(new RustGoliath()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent goliath = findPermanent(player1, "Rust Goliath");
        assertThat(gqs.getEffectivePower(gd, goliath)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, goliath)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, goliath)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, goliath, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, goliath, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void normalCastDealsTenCombatDamage() {
        harness.castFromHand(player1, new RustGoliath(), "{10}");
        harness.passBothPriorities();
        findPermanent(player1, "Rust Goliath").setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 10);
    }

    @Test
    void prototypeDealsThreeCombatDamage() {
        Permanent goliath = castPrototype();
        goliath.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    void prototypeRequiresTwoGreenMana() {
        harness.setHand(player1, List.of(new RustGoliath()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Rust Goliath");
        harness.assertNotOnBattlefield(player1, "Rust Goliath");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void prototypeCanBlockFlyingCreature() {
        Permanent blocker = castPrototype();
        addCreatureReady(player2, new WingCommando());
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void prototypeTramplesOverTwoToughnessBlocker() {
        Permanent goliath = castPrototype();
        goliath.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new WingCommando());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2, player2.getId(), 1));

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Wing Commando");
        harness.assertOnBattlefield(player1, "Rust Goliath");
    }

    @Test
    void normalRecastAfterReturningPrototypeToHandUsesFullSize() {
        Permanent prototype = castPrototype();
        harness.addToBattlefield(player1, new MeticulousExcavation());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, prototype.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Rust Goliath");
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        findPermanent(player1, "Rust Goliath").setSummoningSick(false);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 10);
    }

    private Permanent castPrototype() {
        harness.setHand(player1, List.of(new RustGoliath()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        return findPermanent(player1, "Rust Goliath");
    }
}
