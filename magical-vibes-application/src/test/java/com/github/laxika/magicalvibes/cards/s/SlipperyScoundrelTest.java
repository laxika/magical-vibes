package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ExpelFromOrazca;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlipperyScoundrel.class, Forest.class, GrizzlyBears.class, ExpelFromOrazca.class, TurnToFrog.class})
class SlipperyScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have hexproof or unblockability without the city's blessing")
    void noBlessingNoHexproofOrUnblockability() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());

        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isFalse();
    }

    @Test
    @DisplayName("The city's blessing grants hexproof and unblockability")
    void blessingGrantsHexproofAndUnblockability() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isTrue();
    }

    @Test
    @DisplayName("Entering as the tenth permanent grants the city's blessing")
    void entersAsTenthPermanent() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new SlipperyScoundrel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent scoundrel = findPermanent(player1, "Slippery Scoundrel");
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isTrue();
    }

    @Test
    @DisplayName("Entering as the ninth permanent does not grant the city's blessing")
    void entersBelowAscendThreshold() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        Permanent scoundrel = harness.enterBattlefieldAndReturn(player1, new SlipperyScoundrel());

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isFalse();
    }

    @Test
    @DisplayName("Opponent permanents and blessing do not grant the controller's blessing")
    void opponentsPermanentsAndBlessingDoNotCount() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.enterBattlefieldAndReturn(player2, new SlipperyScoundrel());

        Permanent scoundrel = harness.enterBattlefieldAndReturn(player1, new SlipperyScoundrel());

        assertThat(gd.playersWithCityBlessing).contains(player2.getId()).doesNotContain(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isFalse();
    }

    @Test
    @DisplayName("The blessing and granted abilities persist after dropping below ten permanents")
    void blessingPersistsBelowTenPermanents() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());

        harness.setHand(player1, List.of(new ExpelFromOrazca()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(9);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gqs.hasKeyword(gd, scoundrel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, scoundrel)).isTrue();
    }

    @Test
    @DisplayName("An opponent can target Slippery Scoundrel before the city's blessing")
    void opponentCanTargetWithoutBlessing() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());
        harness.setHand(player2, List.of(new ExpelFromOrazca()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, scoundrel.getId());

        harness.assertNotOnBattlefield(player1, "Slippery Scoundrel");
        harness.assertInHand(player1, "Slippery Scoundrel");
    }

    @Test
    @DisplayName("Hexproof rejects an opponent's spell once the controller has the blessing")
    void opponentCannotTargetWithBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent scoundrel = harness.enterBattlefieldAndReturn(player1, new SlipperyScoundrel());
        harness.setHand(player2, List.of(new ExpelFromOrazca()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, scoundrel.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        harness.assertOnBattlefield(player1, "Slippery Scoundrel");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature can block Slippery Scoundrel without the blessing")
    void canBeBlockedWithoutBlessing() {
        addCreatureReady(player1, new SlipperyScoundrel());
        addCreatureReady(player2, new SlipperyScoundrel());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("A creature cannot block Slippery Scoundrel with the blessing")
    void cannotBeBlockedWithBlessing() {
        addCreatureReady(player1, new SlipperyScoundrel());
        addCreatureReady(player2, new SlipperyScoundrel());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.enterBattlefieldAndReturn(player1, new Forest());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Losing ascend prevents getting the blessing when the tenth permanent enters")
    void lostAscendDoesNotGrantBlessing() {
        Permanent scoundrel = addCreatureReady(player1, new SlipperyScoundrel());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, scoundrel.getId());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(10);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
    }
}
