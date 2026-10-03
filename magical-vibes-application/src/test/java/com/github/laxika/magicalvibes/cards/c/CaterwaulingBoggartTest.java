package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BloodcrazedGoblin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

@CardUsed({CaterwaulingBoggart.class, BloodcrazedGoblin.class, AirElemental.class,
        GrizzlyBears.class, WoodlandChangeling.class})
class CaterwaulingBoggartTest extends BaseCardTest {

    @Test
    @DisplayName("Own Goblin gains menace")
    void ownGoblinGainsMenace() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new BloodcrazedGoblin());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Bloodcrazed Goblin"), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Own Elemental gains menace")
    void ownElementalGainsMenace() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new AirElemental());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Air Elemental"), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Grants menace to itself (it is a Goblin)")
    void grantsMenaceToItself() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Caterwauling Boggart"), Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant menace to own non-Goblin/Elemental creature")
    void doesNotGrantToOtherCreature() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant menace to opponent's Goblin")
    void doesNotGrantToOpponent() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        harness.addToBattlefield(player2, new BloodcrazedGoblin());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Bloodcrazed Goblin"), Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace is lost when Caterwauling Boggart leaves the battlefield")
    void keywordLostWhenLordRemoved() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Caterwauling Boggart"));

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Elemental does not gain menace")
    void doesNotGrantToOpponentElemental() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Changeling entering after the Boggart gains menace")
    void laterChangelingGainsMenace() {
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace remains while another Boggart is on the battlefield")
    void removingOneOfTwoBoggartsKeepsMenace() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CaterwaulingBoggart());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CaterwaulingBoggart());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, changeling, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Boggart cannot be blocked by just one creature")
    void singleBlockerIsRejected() {
        addCreatureReady(player1, new CaterwaulingBoggart());
        addCreatureReady(player2, new WoodlandChangeling());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    @DisplayName("Granted menace allows two creatures to block")
    void twoBlockersAreAllowed() {
        addCreatureReady(player1, new WoodlandChangeling());
        harness.addToBattlefield(player1, new CaterwaulingBoggart());
        Permanent first = addCreatureReady(player2, new WoodlandChangeling());
        Permanent second = addCreatureReady(player2, new WoodlandChangeling());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(first.isBlocking()).isTrue();
        assertThat(second.isBlocking()).isTrue();
    }
}
