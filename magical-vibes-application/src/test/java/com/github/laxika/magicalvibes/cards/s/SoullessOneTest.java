package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.z.ZombieGoliath;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoullessOne.class, ZombieGoliath.class, WalkingCorpse.class, GrizzlyBears.class})
class SoullessOneTest extends BaseCardTest {

    @Test
    @DisplayName("Counts Zombies on the battlefield and Zombie cards in all graveyards")
    void countsZombiesEverywhere() {
        Permanent soullessOne = addSoullessOne(player1);
        harness.addToBattlefield(player1, new ZombieGoliath());
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.setGraveyard(player1, List.of(new ZombieGoliath(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, soullessOne)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts itself and updates when Zombies enter or leave the counted zones")
    void updatesWithZombieCount() {
        Permanent soullessOne = addSoullessOne(player1);

        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(1);

        harness.addToBattlefield(player1, new ZombieGoliath());
        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(2);

        harness.setGraveyard(player2, List.of(new WalkingCorpse(), new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Zombie Goliath"));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        assertThat(gqs.getEffectivePower(gd, soullessOne)).isEqualTo(1);
    }

    private Permanent addSoullessOne(Player player) {
        return addCreatureReady(player, new SoullessOne());
    }
}
