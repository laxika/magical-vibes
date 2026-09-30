package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeethingSkitterPriest.class, GrizzlyBears.class})
class SeethingSkitterPriestTest extends BaseCardTest {

    @Test
    void grantsTheDeathTriggerToControlledCreaturesAndCreatureCardsInHand() {
        Permanent battlefieldBear = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears handBear = new GrizzlyBears();
        harness.setHand(player1, List.of(handBear));
        Permanent priest = harness.enterBattlefieldAndReturn(player1, new SeethingSkitterPriest());
        resolveAllTriggers();

        kill(battlefieldBear);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent handBearPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(handBear.getId()))
                .findFirst()
                .orElseThrow();

        kill(priest);
        harness.passBothPriorities();
        kill(handBearPermanent);
        harness.passBothPriorities();

        List<Permanent> mites = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.MITE))
                .toList();
        assertThat(mites).hasSize(3);
        assertThat(mites).allSatisfy(mite -> assertThat(gqs.hasKeyword(gd, mite, Keyword.TOXIC))
                .isTrue());
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(permanent.getEffectiveToughness());
        harness.runStateBasedActions();
    }
}
