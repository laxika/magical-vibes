package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UrborgElf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CoalitionConstruct.class, GrizzlyBears.class, UrborgElf.class})
class CoalitionConstructTest extends BaseCardTest {

    @Test
    void perpetuallyBoostsMatchingCreaturesAndCreatureCardsInHand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new UrborgElf());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        GrizzlyBears handBear = new GrizzlyBears();

        harness.setHand(player1, List.of(new CoalitionConstruct(), handBear));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.BEAR.name());
        harness.passBothPriorities();

        Permanent construct = findPermanent(player1, "Coalition Construct");
        assertThat(construct.getChosenSubtype()).isEqualTo(CardSubtype.BEAR);
        assertThat(gqs.getEffectivePower(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, construct)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == handBear)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, enteredBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enteredBear)).isEqualTo(3);
    }
}
