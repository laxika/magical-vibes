package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HexgoldSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a creature without toxic")
    void dealsTwoDamageToCreatureWithoutToxic() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castHexgoldSlash(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Deals 4 damage to a creature with toxic instead")
    void dealsFourDamageToCreatureWithToxicInstead() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        target.setToughnessModifier(2);
        castHexgoldSlash(target);

        harness.assertNotOnBattlefield(player2, "Crawling Chorus");
        harness.assertInGraveyard(player2, "Crawling Chorus");
    }

    private void castHexgoldSlash(Permanent target) {
        harness.setHand(player1, List.of(new HexgoldSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
