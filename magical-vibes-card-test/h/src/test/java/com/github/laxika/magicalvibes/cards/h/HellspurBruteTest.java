package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BreechesTheBlastmaker;
import com.github.laxika.magicalvibes.cards.d.DeadeyeDuelist;
import com.github.laxika.magicalvibes.cards.o.OasisGardener;
import com.github.laxika.magicalvibes.cards.o.OverzealousMuscle;
import com.github.laxika.magicalvibes.cards.s.ServantOfTheStinger;
import com.github.laxika.magicalvibes.cards.v.VadmirNewBlood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        HellspurBrute.class,
        OverzealousMuscle.class,
        ServantOfTheStinger.class,
        VadmirNewBlood.class,
        OasisGardener.class,
        DeadeyeDuelist.class,
        BreechesTheBlastmaker.class
})
class HellspurBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for outlaws reduces its generic mana cost")
    void affinityForOutlawsReducesGenericManaCost() {
        harness.addToBattlefield(player1, new OverzealousMuscle());
        harness.addToBattlefield(player1, new OverzealousMuscle());
        harness.addToBattlefield(player1, new ServantOfTheStinger());
        harness.addToBattlefield(player1, new ServantOfTheStinger());
        harness.addToBattlefield(player1, new VadmirNewBlood());
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Affinity counts only outlaws controlled by the spell's controller")
    void affinityCountsOnlyControlledOutlaws() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player2, new OverzealousMuscle());
        }
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Affinity ignores permanents without an outlaw creature type")
    void affinityIgnoresNonOutlaws() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new OasisGardener());
        }
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @MethodSource("outlaws")
    @DisplayName("Each outlaw type reduces the cost by exactly one generic mana")
    void eachOutlawTypeReducesCostByOne(Card outlaw) {
        harness.addToBattlefield(player1, outlaw);
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private static Stream<Card> outlaws() {
        return Stream.of(new DeadeyeDuelist(), new OverzealousMuscle(),
                new BreechesTheBlastmaker(), new VadmirNewBlood(), new ServantOfTheStinger());
    }

    @Test
    @DisplayName("Without outlaws the spell requires its full cost and does not count itself in hand")
    void requiresFullCostWithoutOutlaws() {
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Excess outlaws cannot pay the required red mana")
    void affinityDoesNotReduceColoredMana() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new OverzealousMuscle());
        }
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A tapped Hellspur Brute on the battlefield counts as an outlaw")
    void countsTappedBruteOnBattlefield() {
        Permanent brute = harness.addToBattlefieldAndReturn(player1, new HellspurBrute());
        brute.tap();
        harness.setHand(player1, List.of(new HellspurBrute()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        addCreatureReady(player1, new HellspurBrute());
        Permanent blocker = addCreatureReady(player2, new OasisGardener());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Oasis Gardener");
        harness.assertOnBattlefield(player1, "Hellspur Brute");
    }
}
