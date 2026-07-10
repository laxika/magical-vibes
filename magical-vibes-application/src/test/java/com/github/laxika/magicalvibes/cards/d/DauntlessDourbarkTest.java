package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DauntlessDourbarkTest extends BaseCardTest {

    // ===== P/T = Forests + Treefolk =====

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
        harness.addToBattlefield(player1, createTreefolk());

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
        harness.addToBattlefield(player2, createTreefolk());

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

    // ===== Conditional trample =====

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
        harness.addToBattlefield(player1, createTreefolk());

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Opponent's Treefolk does not grant trample")
    void opponentTreefolkDoesNotGrantTrample() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player2, createTreefolk());

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Loses trample when the other Treefolk leaves")
    void losesTrampleWhenOtherTreefolkLeaves() {
        Permanent dourbark = addDourbarkReady(player1);
        harness.addToBattlefield(player1, createTreefolk());
        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> !p.getCard().getName().equals("Dauntless Dourbark")
                        && p.getCard().getSubtypes().contains(CardSubtype.TREEFOLK));

        assertThat(gqs.hasKeyword(gd, dourbark, Keyword.TRAMPLE)).isFalse();
    }

    // ===== Helpers =====

    private Permanent addDourbarkReady(Player player) {
        Permanent permanent = new Permanent(new DauntlessDourbark());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Card createTreefolk() {
        Card card = new GrizzlyBears();
        card.setSubtypes(List.of(CardSubtype.TREEFOLK));
        return card;
    }
}
