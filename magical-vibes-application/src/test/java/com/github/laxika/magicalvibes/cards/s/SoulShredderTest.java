package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulShredder.class, GrizzlyBears.class, WrathOfGod.class})
class SoulShredderTest extends BaseCardTest {

    @Test
    void getsOnePerpetualBoostForMultipleSimultaneousDeaths() {
        Permanent shredder = harness.addToBattlefieldAndReturn(player1, new SoulShredder());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, shredder)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, shredder)).isEqualTo(3);
    }

    @Test
    void perpetuallyBoostsItsCardWhileInTheGraveyard() {
        SoulShredder shredder = new SoulShredder();
        harness.setGraveyard(player1, List.of(shredder));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Card updatedShredder = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card.getId().equals(shredder.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(updatedShredder.getPower()).isEqualTo(3);
        assertThat(updatedShredder.getToughness()).isEqualTo(3);
    }

    @Test
    void returnsFromTheGraveyardByPayingManaAndSacrificingTwoCreatures() {
        harness.setGraveyard(player1, List.of(new SoulShredder()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soul Shredder")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Soul Shredder"));
    }
}
