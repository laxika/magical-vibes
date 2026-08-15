package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Armageddon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TerraEternalTest extends BaseCardTest {

    @Test
    @DisplayName("All lands have indestructible regardless of who controls them")
    void grantsIndestructibleToAllLands() {
        harness.addToBattlefield(player1, new TerraEternal());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        Permanent ownPlains = findPermanent(player1, "Plains");
        Permanent opponentForest = findPermanent(player2, "Forest");
        assertThat(gqs.hasKeyword(gd, ownPlains, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentForest, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Terra Eternal does not grant indestructible to nonlands")
    void doesNotAffectNonlands() {
        harness.addToBattlefield(player1, new TerraEternal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Lands survive Armageddon while Terra Eternal is on the battlefield")
    void landsSurviveArmageddon() {
        harness.addToBattlefield(player1, new TerraEternal());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new Armageddon()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Terra Eternal");
    }
}
