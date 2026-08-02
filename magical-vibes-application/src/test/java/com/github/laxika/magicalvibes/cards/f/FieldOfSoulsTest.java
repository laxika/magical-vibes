package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FieldOfSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("Your nontoken creature dying creates a 1/1 white Spirit with flying")
    void allyCreatureDeathCreatesSpirit() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player1, new GrizzlyBears());

        wrathFromOpponent();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Two of your creatures dying creates two Spirits")
    void twoDeathsCreateTwoSpirits() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's creature dying does not create a Spirit")
    void opponentCreatureDeathCreatesNothing() {
        harness.addToBattlefield(player1, new FieldOfSouls());
        harness.addToBattlefield(player2, new GrizzlyBears());

        wrathFromOpponent();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    /**
     * Has player2 cast Wrath of God and resolves it plus any resulting triggers. Field of Souls is
     * an enchantment, so it survives the board wipe.
     */
    private void wrathFromOpponent() {
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();

        while (!harness.getGameData().stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
