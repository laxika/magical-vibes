package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.cards.r.RiparianTiger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceExtraction.class, PrakhataPillarBug.class, RiparianTiger.class})
class EssenceExtractionTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToTargetCreatureAndGainsThreeLife() {
        harness.addToBattlefield(player2, new PrakhataPillarBug());
        harness.setHand(player1, List.of(new EssenceExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Prakhata Pillar-Bug"));

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Prakhata Pillar-Bug");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new EssenceExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsNoLifeWhenTargetIsIllegalOnResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PrakhataPillarBug());
        harness.setHand(player1, List.of(new EssenceExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 15);

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    void survivingCreatureHasThreeDamageAndOnlyCasterGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RiparianTiger());
        harness.setHand(player1, List.of(new EssenceExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 15);
        harness.setLife(player2, 12);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Riparian Tiger");
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 12);
    }

    @Test
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PrakhataPillarBug());
        harness.setHand(player1, List.of(new EssenceExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Prakhata Pillar-Bug");
        harness.assertLife(player1, 18);
    }

    @Test
    void gainsThreeLifeEvenWhenAllDamageIsPrevented() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PrakhataPillarBug());
        creature.setDamagePreventionShield(3);
        harness.setHand(player1, List.of(new EssenceExtraction()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 15);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Prakhata Pillar-Bug");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }
}
