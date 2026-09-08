package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FountainWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts and enchantments you control have shroud")
    void artifactsAndEnchantmentsYouControlHaveShroud() {
        harness.addToBattlefield(player1, new FountainWatch());
        harness.addToBattlefield(player1, new IcyManipulator());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Icy Manipulator"), Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Glorious Anthem"), Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Creatures without artifact or enchantment types are unaffected")
    void otherPermanentsAreUnaffected() {
        harness.addToBattlefield(player1, new FountainWatch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Icy Manipulator"), Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Granted shroud is lost when Fountain Watch leaves the battlefield")
    void shroudIsLostWhenFountainWatchLeaves() {
        harness.addToBattlefield(player1, new FountainWatch());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent fountainWatch = findPermanent(player1, "Fountain Watch");
        Permanent icyManipulator = findPermanent(player1, "Icy Manipulator");

        assertThat(gqs.hasKeyword(gd, icyManipulator, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(fountainWatch);

        assertThat(gqs.hasKeyword(gd, icyManipulator, Keyword.SHROUD)).isFalse();
    }
}
