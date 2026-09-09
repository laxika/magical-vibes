package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamblingGhast.class, BoneSplinters.class, AirElemental.class})
class ShamblingGhastTest extends BaseCardTest {

    @Test
    void createsTreasureTokenWhenChosen() {
        Permanent ghast = addGhastAndTarget();

        castBoneSplintersSacrificing(ghast);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a Treasure token.");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void weakensTargetCreatureAnOpponentControlsWhenChosen() {
        Permanent ghast = addGhastAndTarget();
        Permanent target = findPermanent(player2, "Air Elemental");

        castBoneSplintersSacrificing(ghast);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Target creature an opponent controls gets -1/-1 until end of turn.");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    private Permanent addGhastAndTarget() {
        Permanent ghast = harness.addToBattlefieldAndReturn(player1, new ShamblingGhast());
        harness.addToBattlefield(player2, new AirElemental());
        return ghast;
    }

    private void castBoneSplintersSacrificing(Permanent ghast) {
        harness.setHand(player1, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0,
                findPermanent(player2, "Air Elemental").getId(), ghast.getId());
    }
}
