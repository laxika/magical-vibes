package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.h.HealingSalve;
import com.github.laxika.magicalvibes.cards.p.PheresBandThunderhoof;
import com.github.laxika.magicalvibes.cards.s.Starfall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DictateOfTheTwinGods.class, Blaze.class, GoldenHind.class,
        HealingSalve.class, PheresBandThunderhoof.class, Starfall.class})
class DictateOfTheTwinGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles damage from any source to a player")
    void doublesDamageToPlayer() {
        harness.addToBattlefield(player1, new DictateOfTheTwinGods());
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, 3, player1.getId());

        harness.assertLife(player1, 14);
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new DictateOfTheTwinGods()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.ensurePriority(player1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dictate of the Twin Gods");
    }

    @Test
    void doublesSpellDamageToOpposingCreature() {
        harness.addToBattlefield(player1, new DictateOfTheTwinGods());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PheresBandThunderhoof());
        harness.setHand(player1, List.of(new Starfall()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Pheres-Band Thunderhoof");
        harness.assertInGraveyard(player2, "Pheres-Band Thunderhoof");
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleCopiesMultiplyDamageRegardlessOfController() {
        harness.addToBattlefield(player1, new DictateOfTheTwinGods());
        harness.addToBattlefield(player2, new DictateOfTheTwinGods());
        addCreatureReady(player1, new GoldenHind());

        declareAttackers(player1, List.of(1));
        resolveCombat(player1);

        harness.assertLife(player2, 12);
    }

    @Test
    void doublesCombatDamageToCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new DictateOfTheTwinGods());
        addCreatureReady(player1, new GoldenHind());
        addCreatureReady(player2, new PheresBandThunderhoof());

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat(player1);

        harness.assertInGraveyard(player1, "Golden Hind");
        harness.assertInGraveyard(player2, "Pheres-Band Thunderhoof");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotDoubleDamageWhileOnlyInHand() {
        harness.setHand(player1, List.of(new DictateOfTheTwinGods()));
        addCreatureReady(player2, new GoldenHind());

        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        harness.assertLife(player1, 18);
    }

    @Test
    void affectedPlayerChoosesBetweenPreventionAndDoublingOrder() {
        harness.addToBattlefield(player1, new DictateOfTheTwinGods());
        harness.setHand(player2, List.of(new HealingSalve()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castInstant(player2, 0, 1, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castAndResolveSorcery(player1, 0, 4, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
    }
}