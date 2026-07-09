package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BloodcrazedGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaterwaulingBoggartTest extends BaseCardTest {

    private Permanent find(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Own Goblin gains menace")
    void ownGoblinGainsMenace() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new BloodcrazedGoblin());

        assertThat(gqs.hasKeyword(gd, find(player1, "Bloodcrazed Goblin"), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Own Elemental gains menace")
    void ownElementalGainsMenace() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.hasKeyword(gd, find(player1, "Air Elemental"), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Grants menace to itself (it is a Goblin)")
    void grantsMenaceToItself() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());

        assertThat(gqs.hasKeyword(gd, find(player1, "Caterwauling Boggart"), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant menace to own non-Goblin/Elemental creature")
    void doesNotGrantToOtherCreature() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, find(player1, "Grizzly Bears"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant menace to opponent's Goblin")
    void doesNotGrantToOpponent() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player2, new BloodcrazedGoblin());

        assertThat(gqs.hasKeyword(gd, find(player2, "Bloodcrazed Goblin"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace is lost when Caterwauling Boggart leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new AirElemental());

        Permanent elemental = find(player1, "Air Elemental");
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Caterwauling Boggart"));

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.MENACE)).isFalse();
    }
}
