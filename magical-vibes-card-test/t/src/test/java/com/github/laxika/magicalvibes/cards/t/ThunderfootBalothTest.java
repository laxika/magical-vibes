package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EzuriRenegadeLeader;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderfootBaloth.class, EzuriRenegadeLeader.class, LlanowarElves.class})
class ThunderfootBalothTest extends BaseCardTest {

    @Test
    void lieutenantBoostsSourceAndOtherControlledCreatures() {
        addCommanderToBattlefield(player1);
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Permanent opposingElves = addCreatureReady(player2, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, baloth, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingElves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingElves)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingElves, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void withoutCommanderLieutenantDoesNotApply() {
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void controllingOpponentsCommanderDoesNotEnableLieutenant() {
        Card commander = new EzuriRenegadeLeader();
        gd.makeCommander(player2.getId(), commander);
        addCreatureReady(player1, commander);
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void commanderEnteringAndLeavingUpdatesLieutenantImmediately() {
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card commander = new EzuriRenegadeLeader();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();

        gd.playerCommandZones.get(player1.getId()).remove(commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, commanderPermanent)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commanderPermanent, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerBattlefields.get(player2.getId()).add(commanderPermanent);

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, baloth, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void anotherCardWithCommandersNameDoesNotEnableLieutenant() {
        gd.makeCommander(player1.getId(), new EzuriRenegadeLeader());
        addCreatureReady(player1, new EzuriRenegadeLeader());
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(5);
    }

    @Test
    void removingBalothRemovesItsBonusFromOtherCreatures() {
        addCommanderToBattlefield(player1);
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(baloth);
        gd.playerGraveyards.get(player1.getId()).add(baloth.getCard());

        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elves)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void changingBalothsControllerChecksNewControllersCommander() {
        addCommanderToBattlefield(player1);
        Permanent baloth = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent elves = addCreatureReady(player2, new LlanowarElves());

        gd.playerBattlefields.get(player1.getId()).remove(baloth);
        gd.playerBattlefields.get(player2.getId()).add(baloth);

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isFalse();

        addCommanderToBattlefield(player2);

        assertThat(gqs.getEffectivePower(gd, baloth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, baloth)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, elves)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, elves, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void multipleBalothsBoostEachOtherButNotThemselvesTwice() {
        addCommanderToBattlefield(player1);
        Permanent first = addCreatureReady(player1, new ThunderfootBaloth());
        Permanent second = addCreatureReady(player1, new ThunderfootBaloth());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(9);
    }

    private void addCommanderToBattlefield(Player player) {
        Card commander = new EzuriRenegadeLeader();
        gd.makeCommander(player.getId(), commander);
        addCreatureReady(player, commander);
    }
}
