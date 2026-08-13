package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VanguardOfBrimazTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Vanguard of Brimaz creates a vigilant Cat Soldier token")
    void targetingVanguardCreatesCatSoldier() {
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID vanguardId = harness.getPermanentId(player1, "Vanguard of Brimaz");
        harness.castInstant(player1, 0, vanguardId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Soldier")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Cat Soldier");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("A spell that does not target Vanguard of Brimaz does not create a token")
    void spellNotTargetingVanguardDoesNotCreateCatSoldier() {
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Soldier")).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Vanguard of Brimaz does not create a token")
    void opponentsSpellDoesNotCreateCatSoldier() {
        harness.addToBattlefield(player1, new VanguardOfBrimaz());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID vanguardId = harness.getPermanentId(player1, "Vanguard of Brimaz");
        harness.castInstant(player2, 0, vanguardId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat Soldier")).isZero();
    }
}
