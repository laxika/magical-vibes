package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DiffusionSliver;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomSliver.class, DiffusionSliver.class, RuneclawBear.class})
class VenomSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Venom Sliver grants itself deathtouch (it is a Sliver)")
    void grantsSelfDeathtouch() {
        Permanent sliver = addCreatureReady(player1, new VenomSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Grants deathtouch to another Sliver you control")
    void grantsDeathtouchToOtherSliver() {
        addCreatureReady(player1, new VenomSliver());
        Permanent otherSliver = addCreatureReady(player1, new DiffusionSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch is revoked when Venom Sliver leaves the battlefield")
    void revokesWhenSourceLeaves() {
        Permanent sliver = addCreatureReady(player1, new VenomSliver());
        Permanent otherSliver = addCreatureReady(player1, new DiffusionSliver());

        gd.playerBattlefields.get(player1.getId()).remove(sliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new VenomSliver());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new VenomSliver());
        Permanent opponentSliver = addCreatureReady(player2, new DiffusionSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Venom Sliver's one combat damage destroys a creature with greater toughness")
    void selfDeathtouchDestroysLargerBlocker() {
        addCreatureReady(player1, new VenomSliver());
        addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Venom Sliver");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertNotOnBattlefield(player1, "Venom Sliver");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Another Sliver's granted deathtouch destroys a larger attacker while blocking")
    void grantedDeathtouchDestroysLargerAttacker() {
        addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new VenomSliver());
        addCreatureReady(player2, new DiffusionSliver());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Diffusion Sliver");
        harness.assertOnBattlefield(player2, "Venom Sliver");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deathtouch persists while another Venom Sliver remains")
    void deathtouchPersistsUntilLastSourceLeaves() {
        Permanent first = addCreatureReady(player1, new VenomSliver());
        Permanent second = addCreatureReady(player1, new VenomSliver());
        Permanent otherSliver = addCreatureReady(player1, new DiffusionSliver());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, second, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DEATHTOUCH)).isFalse();
    }
}
