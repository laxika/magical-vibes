package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KoboldOverlord.class, KherKeep.class, GrizzlyBears.class})
class KoboldOverlordTest extends BaseCardTest {

    @Test
    @DisplayName("Other Kobold creatures you control have first strike")
    void grantsFirstStrikeToOtherKoboldsYouControl() {
        Permanent ownKobold = createKoboldToken(player1);
        Permanent opponentKobold = createKoboldToken(player2);
        Permanent ownNonKobold = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.addToBattlefield(player1, new KoboldOverlord());

        assertThat(gqs.hasKeyword(gd, ownKobold, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentKobold, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownNonKobold, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Kobolds entering later also gain first strike")
    void affectsKoboldsEnteringLater() {
        harness.addToBattlefield(player1, new KoboldOverlord());

        Permanent ownKobold = createKoboldToken(player1);

        assertThat(gqs.hasKeyword(gd, ownKobold, Keyword.FIRST_STRIKE)).isTrue();
    }

    private Permanent createKoboldToken(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new KherKeep());
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player, "Kobolds of Kher Keep");
    }
}
