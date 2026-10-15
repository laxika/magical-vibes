package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FaithbearerPaladin;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkovCrusader.class, FaithbearerPaladin.class})
class MarkovCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Markov Crusader has haste when you control another Vampire")
    void hasHasteWhenControllerControlsAnotherVampire() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());

        assertThat(gqs.hasKeyword(gd, vampire, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Markov Crusader does not have haste without another Vampire")
    void noHasteWithoutAnotherVampire() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A non-Vampire does not grant Markov Crusader haste")
    void noHasteWithNonVampire() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());
        harness.addToBattlefield(player1, new FaithbearerPaladin());

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Markov Crusader loses haste when the other Vampire leaves")
    void losesHasteWhenOtherVampireLeaves() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());

        assertThat(gqs.hasKeyword(gd, vampire, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(vampire);

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentsVampireDoesNotGrantHaste() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());
        harness.addToBattlefield(player2, new MarkovCrusader());

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.HASTE)).isFalse();
    }

    @Test
    void newlyControlledCrusaderCanAttackAndGainLifeWithAnotherVampire() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());
        crusader.setSummoningSick(true);
        harness.addToBattlefield(player1, new MarkovCrusader());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(crusader)));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    void summoningSickCrusaderCannotAttackWithoutAnotherVampire() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new MarkovCrusader());
        crusader.setSummoningSick(true);

        assertThat(als.canAttack(gd, crusader, player1.getId())).isFalse();
    }
}
