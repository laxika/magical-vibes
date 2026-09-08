package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LanternSpirit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SekkiSeasonsGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with eight +1/+1 counters")
    void entersWithEightCounters() {
        harness.setHand(player1, List.of(new SekkiSeasonsGuide()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sekki = findPermanent(player1, "Sekki, Seasons' Guide");
        assertThat(sekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Prevents damage, removes matching counters, and creates colorless Spirit tokens")
    void preventsDamageRemovesCountersAndCreatesTokens() {
        Permanent sekki = harness.addToBattlefieldAndReturn(player2, new SekkiSeasonsGuide());
        sekki.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 8);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sekki.getId());
        harness.passBothPriorities();

        Permanent survivingSekki = findPermanent(player2, "Sekki, Seasons' Guide");
        assertThat(survivingSekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(countSpiritTokens(player2)).isEqualTo(2);
        assertThat(survivingSekki.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Creates tokens for all prevented damage even when counters run out")
    void createsTokensForDamageBeyondCounterCount() {
        Permanent sekki = harness.addToBattlefieldAndReturn(player2, new SekkiSeasonsGuide());
        sekki.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, sekki.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Sekki, Seasons' Guide");
        assertThat(countSpiritTokens(player2)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Sekki, Seasons' Guide");
    }

    @Test
    @DisplayName("Sacrificing eight Spirits returns Sekki from the graveyard")
    void sacrificesEightSpiritsToReturnFromGraveyard() {
        SekkiSeasonsGuide sekki = new SekkiSeasonsGuide();
        harness.setGraveyard(player1, List.of(sekki));

        List<UUID> spiritIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            spiritIds.add(harness.addToBattlefieldAndReturn(player1, new LanternSpirit()).getId());
        }

        harness.activateGraveyardAbility(player1, 0);
        for (UUID spiritId : spiritIds) {
            if (!gd.interaction.isAwaitingInput()) {
                break;
            }
            harness.handlePermanentChosen(player1, spiritId);
        }
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(sekki.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Lantern Spirit"));
        Permanent returnedSekki = findPermanent(player1, "Sekki, Seasons' Guide");
        assertThat(returnedSekki.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    private long countSpiritTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .count();
    }
}
