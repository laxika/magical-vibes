package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StoicSphinx.class, Shock.class})
class StoicSphinxTest extends BaseCardTest {

    @Test
    void hasHexproofBeforeItsControllerCastsASpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void losesHexproofAfterItsControllerCastsASpell() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new StoicSphinx());
        harness.setHand(player1, java.util.List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void doesNotHaveHexproofWhenItWasCastThisTurn() {
        harness.setHand(player1, java.util.List.of(new StoicSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sphinx = findPermanent(player1, "Stoic Sphinx");
        assertThat(gqs.hasKeyword(gd, sphinx, Keyword.HEXPROOF)).isFalse();
    }
}
