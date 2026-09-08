package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DarienKingOfKjeldorTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to you lets you create that many Soldier tokens when accepted")
    void damageCreatesThatManySoldierTokensWhenAccepted() {
        harness.addToBattlefield(player1, new DarienKingOfKjeldor());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(3);
        assertThat(soldiers).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Declining the damage trigger creates no Soldier tokens")
    void decliningCreatesNoSoldierTokens() {
        harness.addToBattlefield(player1, new DarienKingOfKjeldor());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }
}
