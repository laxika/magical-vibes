package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PheresBandBrawler.class, GrizzlyBears.class})
class PheresBandBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB fights up to one target creature an opponent controls")
    void entersAndFightsTargetCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent brawler = castBrawler();

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(brawler.getId()));
    }

    @Test
    @DisplayName("ETB resolves without a target when an opponent controls no creatures")
    void entersWithoutTarget() {
        Permanent brawler = castBrawler();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(brawler.getId()));
    }

    private Permanent castBrawler() {
        harness.setHand(player1, List.of(new PheresBandBrawler()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Pheres-Band Brawler");
    }
}
