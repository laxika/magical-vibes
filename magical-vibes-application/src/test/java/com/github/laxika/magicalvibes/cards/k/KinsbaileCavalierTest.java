package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KinsbaileCavalierTest extends BaseCardTest {

    private Permanent find(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Own Knight gains double strike")
    void ownKnightGainsDoubleStrike() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, find(player1, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants double strike to itself (it is a Knight)")
    void grantsDoubleStrikeToItself() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());

        assertThat(gqs.hasKeyword(gd, find(player1, "Kinsbaile Cavalier"), Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant double strike to own non-Knight creature")
    void doesNotGrantToNonKnight() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, find(player1, "Grizzly Bears"), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to opponent's Knight")
    void doesNotGrantToOpponent() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player2, new BenalishKnight());

        assertThat(gqs.hasKeyword(gd, find(player2, "Benalish Knight"), Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double strike is lost when Kinsbaile Cavalier leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new KinsbaileCavalier());
        harness.addToBattlefield(player1, new BenalishKnight());

        Permanent knight = find(player1, "Benalish Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Kinsbaile Cavalier"));

        assertThat(gqs.hasKeyword(gd, knight, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
