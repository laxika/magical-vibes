package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnabaBodyguard.class, RagingGoblin.class, GrizzlyBears.class, HillGiant.class})
class AnabaBodyguardTest extends BaseCardTest {

    @Test
    @DisplayName("First strike lets Anaba Bodyguard survive a lethal blocker")
    void firstStrikeDealsDamageBeforeBlocker() {
        addCreatureReady(player1, new AnabaBodyguard());
        RagingGoblin blockerCard = new RagingGoblin();
        blockerCard.setPower(3);
        blockerCard.setToughness(1);
        addCreatureReady(player2, blockerCard);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Anaba Bodyguard");
        harness.assertInGraveyard(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("First strike kills an attacker before it damages Anaba Bodyguard")
    void firstStrikeWorksWhileBlocking() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new AnabaBodyguard());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Anaba Bodyguard");
        assertThat(findPermanent(player2, "Anaba Bodyguard").getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A blocker that survives first strike still deals regular combat damage")
    void survivingBlockerDealsDamageBack() {
        addCreatureReady(player1, new AnabaBodyguard());
        addCreatureReady(player2, new HillGiant());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Anaba Bodyguard");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Anaba Bodyguard deals damage only once")
    void firstStrikeDoesNotDealRegularDamageAgain() {
        addCreatureReady(player1, new AnabaBodyguard());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
