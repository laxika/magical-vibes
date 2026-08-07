package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BudokaGardener;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DokaiWeaverOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Elemental token whose power and toughness equal the lands controlled")
    void tokenSizeMatchesLandCount() {
        addTransformedGardener(player1);
        addForests(player1, 7);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(7);
    }

    @Test
    @DisplayName("Opponent lands do not count toward the token's size")
    void opponentLandsDoNotCount() {
        addTransformedGardener(player1);
        addForests(player1, 3);
        addForests(player2, 5);
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
    }

    private Permanent addTransformedGardener(Player player) {
        BudokaGardener card = new BudokaGardener();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            gd.playerBattlefields.get(player.getId()).add(new Permanent(new Forest()));
        }
    }
}
