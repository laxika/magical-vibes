package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeistySpikeling.class})
class FeistySpikelingTest extends BaseCardTest {

    @Test
    @DisplayName("Has first strike during its controller's turn")
    void hasFirstStrikeDuringControllerTurn() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());
        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not have first strike during an opponent's turn")
    void doesNotHaveFirstStrikeDuringOpponentTurn() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());
        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike updates as the active player changes")
    void firstStrikeUpdatesWhenTurnChanges() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike follows the current controller rather than the owner")
    void firstStrikeFollowsCurrentController() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());
        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(spikeling);
        gd.playerBattlefields.get(player2.getId()).add(spikeling);
        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, spikeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An attacking Spikeling kills a defending Spikeling before it deals damage")
    void attackingSpikelingDealsFirstStrikeDamage() {
        Permanent attacker = addCreatureReady(player1, new FeistySpikeling());
        Permanent blocker = addCreatureReady(player2, new FeistySpikeling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertInGraveyard(player2, "Feisty Spikeling");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Changeling matches different creature types but not land types")
    void changelingMatchesCreatureTypes() {
        Permanent spikeling = addCreatureReady(player1, new FeistySpikeling());

        assertThat(gqs.hasEffectiveSubtype(gd, spikeling, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, spikeling, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, spikeling, CardSubtype.FOREST)).isFalse();
    }
}
