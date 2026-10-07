package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnuridMurkdiver;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoullessOne.class, AnuridMurkdiver.class, ElvishWarrior.class})
class SoullessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Counts Zombies on the battlefield and Zombie cards in all graveyards")
    void countsZombiesEverywhere() {
        Permanent soullessOne = addSoullessOne(player1);
        harness.addToBattlefield(player1, new AnuridMurkdiver());
        harness.addToBattlefield(player2, new AnuridMurkdiver());
        harness.setGraveyard(player1, List.of(new AnuridMurkdiver(), new ElvishWarrior()));
        harness.setGraveyard(player2, List.of(new AnuridMurkdiver(), new ElvishWarrior()));

        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, soullessOne)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts itself and updates when Zombies enter or leave the counted zones")
    void updatesWithZombieCount() {
        Permanent soullessOne = addSoullessOne(player1);

        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(1);

        harness.addToBattlefield(player1, new AnuridMurkdiver());
        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new AnuridMurkdiver(), new ElvishWarrior()));
        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Anurid Murkdiver"));
        harness.setGraveyard(player2, List.of(new ElvishWarrior()));
        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ignores non-Zombies on the battlefield and Zombies in hand, library, and exile")
    void ignoresUncountedCardsAndZones() {
        Permanent soullessOne = addSoullessOne(player1);
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        for (Player player : List.of(player1, player2)) {
            harness.setHand(player, List.of(new AnuridMurkdiver(), new SoullessOne()));
            harness.setLibrary(player, List.of(new AnuridMurkdiver(), new SoullessOne()));
            harness.setExile(player, List.of(new AnuridMurkdiver(), new SoullessOne()));
        }

        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soullessOne)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Soulless Ones each count all copies on the battlefield and in graveyards")
    void countsOtherCopiesWithoutRecursion() {
        Permanent first = addSoullessOne(player1);
        Permanent second = addSoullessOne(player2);
        harness.setGraveyard(player1, List.of(new SoullessOne()));
        harness.setGraveyard(player2, List.of(new SoullessOne()));

        for (Permanent permanent : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(4);
        }
    }

    private Permanent addSoullessOne(Player player) {
        return addCreatureReady(player, new SoullessOne());
    }
}
