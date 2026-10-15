package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DauntlessDourbark.class, Forest.class, WoodlandChangeling.class, Lignify.class})
class DauntlessDourbarkTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a Treefolk when alone: 1/1")
    void countsItselfAsTreefolk() {
        Permanent dourbark = addDourbarkReady(player1);

        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals Forests you control plus Treefolk you control")
    void ptEqualsForestsPlusTreefolk() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new DauntlessDourbark());

        // 2 Forests + 2 Treefolk (itself + the added one)
        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only your Forests and Treefolk, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new DauntlessDourbark());

        // 1 own Forest + 1 Treefolk (itself)
        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(2);
    }

    @Test
    @DisplayName("P/T updates when Forests change")
    void ptUpdatesWhenForestsChange() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player1, new Forest());
        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Forest"));
        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(1);
    }

    @Test
    @DisplayName("No trample when it is the only Treefolk you control")
    void noTrampleWhenOnlyTreefolk() {
        Permanent dourbark = addDourbarkReady(player1);

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Has trample as long as you control another Treefolk")
    void hasTrampleWithAnotherTreefolk() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player1, new DauntlessDourbark());

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Treefolk does not grant trample")
    void opponentTreefolkDoesNotGrantTrample() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player2, new DauntlessDourbark());

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses trample when the other Treefolk leaves")
    void losesTrampleWhenOtherTreefolkLeaves() {
        Permanent dourbark = addDourbarkReady(player1);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DauntlessDourbark());
        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(other);

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addDourbarkReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DauntlessDourbark());
        permanent.setSummoningSick(false);
        return permanent;
    }

    @Test
    @DisplayName("Changeling counts as another Treefolk and grants trample")
    void changelingCountsAsTreefolk() {
        Permanent dourbark = addDourbarkReady(player1);
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(changeling);

        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A Treefolk Aura counts even though it is not a creature; ability removal overrides Dourbark")
    void treefolkAuraCountsAndLignifyRemovesAbilities() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player1, new Forest());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DauntlessDourbark());
        Permanent lignify = harness.addToBattlefieldAndReturn(player1, new Lignify());
        lignify.setAttachedTo(dourbark.getId());

        assertThat(gqs.getEffectivePower(gd, dourbark)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The characteristic ability works in hand without counting the card itself")
    void characteristicAbilityWorksInHand() {
        DauntlessDourbark card = new DauntlessDourbark();
        harness.setHand(player1, List.of(card));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isZero();

        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving Dourbark counts itself and the Forest already on the battlefield")
    void castingAndResolvingCountsItself() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new DauntlessDourbark(), "{3}{G}");
        harness.passBothPriorities();

        Permanent dourbark = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof DauntlessDourbark)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, dourbark)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, dourbark)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }
}
