package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldOverlord.class, KoboldsOfKherKeep.class, BarbaryApes.class})
class KoboldOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobold creatures you control have first strike")
    void grantsFirstStrikeToOtherKoboldsYouControl() {
        Permanent ownKobold = createKobold(player1);
        Permanent opponentKobold = createKobold(player2);
        Permanent ownNonKobold = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());

        harness.addToBattlefield(player1, new KoboldOverlord());

        assertThat(gqs.hasKeyword(gd, ownKobold, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentKobold, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownNonKobold, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Kobolds entering later also gain first strike")
    void affectsKoboldsEnteringLater() {
        harness.addToBattlefield(player1, new KoboldOverlord());

        Permanent ownKobold = createKobold(player1);

        assertThat(gqs.hasKeyword(gd, ownKobold, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent createKobold(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KoboldsOfKherKeep());
    }
}
