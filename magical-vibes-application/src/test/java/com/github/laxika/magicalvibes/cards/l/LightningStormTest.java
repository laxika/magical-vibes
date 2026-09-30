package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DeepfireElemental;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningStorm.class, SnowCoveredForest.class, DeepfireElemental.class})
class LightningStormTest extends BaseCardTest {

    @Test
    void dealsThreeDamage() {
        harness.setHand(player1, List.of(new LightningStorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void dealsDamageToACreatureTarget() {
        Permanent target = addCreatureReady(player2, new DeepfireElemental());
        harness.setHand(player1, List.of(new LightningStorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void anyPlayerMayDiscardALandToAddChargeCounters() {
        LightningStorm storm = new LightningStorm();
        harness.setHand(player1, List.of(storm));
        harness.setHand(player2, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.activateStackAbility(player2, storm.getId(), 0, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack.getFirst().getCounterCount(CounterType.CHARGE))
                .isEqualTo(2);
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Snow-Covered Forest");
    }

    @Test
    void cannotActivateWithANonlandCard() {
        LightningStorm storm = new LightningStorm();
        harness.setHand(player1, List.of(storm));
        harness.setHand(player2, List.of(new LightningStorm()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateStackAbility(player2, storm.getId(), 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack.getFirst().getCounterCount(CounterType.CHARGE)).isZero();
        harness.assertInHand(player2, "Lightning Storm");

        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    void mayRetargetsTheSpell() {
        LightningStorm storm = new LightningStorm();
        harness.setHand(player1, List.of(storm));
        harness.setHand(player2, List.of(new SnowCoveredForest()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player1.getId());
        harness.passPriority(player1);
        harness.activateStackAbility(player2, storm.getId(), 0, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
    }
}
