package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.r.Retromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantEmberkin.class, GrizzlyBears.class, Retromancer.class, Humble.class})
class ValiantEmberkinTest extends BaseCardTest {

    @Test
    void entersAndPerpetuallyBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValiantEmberkin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void attacksAndPerpetuallyBoostsTargetCreature() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void doublesTriggeredAbilitiesWhenAnAllyCreatureBecomesTargeted() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent retromancer = addCreatureReady(player1, new Retromancer());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, retromancer.getId());

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals(retromancer.getCard().getName()))
                .count()).isEqualTo(2);

        resolveAllTriggers();
        harness.assertLife(player2, 14);
    }
}
