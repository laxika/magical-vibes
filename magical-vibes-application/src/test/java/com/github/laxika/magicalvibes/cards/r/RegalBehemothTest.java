package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RegalBehemoth.class, Forest.class})
class RegalBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new RegalBehemoth());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Adds one mana while its controller is the monarch")
    void addsManaWhileControllerIsMonarch() {
        harness.addToBattlefield(player1, new RegalBehemoth());
        gd.monarchPlayerId = player1.getId();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not add mana while its controller is not the monarch")
    void doesNotAddManaWithoutMonarch() {
        harness.addToBattlefield(player1, new RegalBehemoth());
        gd.monarchPlayerId = player2.getId();
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
