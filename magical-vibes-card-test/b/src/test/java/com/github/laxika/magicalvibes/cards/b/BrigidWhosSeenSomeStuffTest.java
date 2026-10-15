package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KithkinGreatheart;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrigidWhosSeenSomeStuff.class, KithkinGreatheart.class, GrizzlyBears.class, HillGiant.class,
        KnightOfMeadowgrain.class})
class BrigidWhosSeenSomeStuffTest extends BaseCardTest {

    @Test
    @DisplayName("Thoughtweft gives your Kithkin vigilance and nimble")
    void sharesPrintedKeywordsWithKithkin() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent kithkin = addCreatureReady(player1, new KithkinGreatheart());
        Permanent nonKithkin = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, brigid, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, brigid, Keyword.NIMBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kithkin, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kithkin, Keyword.NIMBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonKithkin, Keyword.NIMBLE)).isFalse();
    }

    @Test
    @DisplayName("Nimble prevents blocking by creatures with power 3 or greater")
    void nimblePreventsHighPowerBlockers() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        declareAttackAndPrepareBlockers(brigid);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(brigid)
        )))).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(nimble)");
    }

    @Test
    @DisplayName("Nimble allows blocking by creatures with power 2 or less")
    void nimbleAllowsLowPowerBlockers() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackAndPrepareBlockers(brigid);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(brigid)
        )));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Thoughtweft shares other Kithkin's printed first strike and lifelink")
    void sharesOtherKithkinsPrintedKeywords() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent knight = addCreatureReady(player1, new KnightOfMeadowgrain());
        Permanent greatheart = addCreatureReady(player1, new KithkinGreatheart());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, brigid, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, brigid, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.NIMBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Kithkin neither receive nor contribute thoughtweft keywords")
    void thoughtweftIsLimitedToYourCreatures() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent knight = addCreatureReady(player2, new KnightOfMeadowgrain());
        Permanent greatheart = addCreatureReady(player2, new KithkinGreatheart());

        assertThat(gqs.hasKeyword(gd, brigid, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, brigid, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.NIMBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.NIMBLE)).isFalse();
    }

    @Test
    @DisplayName("Thoughtweft ignores first strike gained from a conditional static ability")
    void doesNotShareConditionallyGainedKeywords() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent greatheart = addCreatureReady(player1, new KithkinGreatheart());
        addCreatureReady(player1, new HillGiant());

        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, brigid, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Shared keywords disappear when Brigid leaves the battlefield")
    void sharedKeywordsEndWhenBrigidLeaves() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent greatheart = addCreatureReady(player1, new KithkinGreatheart());
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.NIMBLE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, brigid));

        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, greatheart, Keyword.NIMBLE)).isFalse();
    }

    @Test
    @DisplayName("Brigid and another Kithkin attack without tapping from vigilance")
    void sharedVigilancePreventsTappingOnAttack() {
        Permanent brigid = addCreatureReady(player1, new BrigidWhosSeenSomeStuff());
        Permanent greatheart = addCreatureReady(player1, new KithkinGreatheart());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0, 1));

        assertThat(brigid.isAttacking()).isTrue();
        assertThat(greatheart.isAttacking()).isTrue();
        assertThat(brigid.isTapped()).isFalse();
        assertThat(greatheart.isTapped()).isFalse();
    }

    private void declareAttackAndPrepareBlockers(Permanent attacker) {
        attacker.setAttacking(true);
        prepareDeclareBlockers();
    }
}
