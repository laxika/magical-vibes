package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheColossalDreadmaw.class, ColossalDreadmaw.class, HillGiant.class, DarkRitual.class})
class TheColossalDreadmawTest extends BaseCardTest {

    @Test
    void castsCreatureCardsAsColossalDreadmaw() {
        harness.addToBattlefieldAndReturn(player1, new TheColossalDreadmaw());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dreadmaw = findPermanent(player1, "Colossal Dreadmaw");
        assertThat(gqs.getEffectivePower(gd, dreadmaw)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, dreadmaw)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, dreadmaw, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void doesNotChangeNoncreatureCardsInHand() {
        harness.addToBattlefieldAndReturn(player1, new TheColossalDreadmaw());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dark Ritual");
    }
}
