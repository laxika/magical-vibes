package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PestbroodSlothTest extends BaseCardTest {

    @Test
    @DisplayName("When Pestbrood Sloth dies, it creates two Pest tokens")
    void deathCreatesTwoPests() {
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();

        assertThat(findPermanents(player1, "Pest")).hasSize(2);
    }

    @Test
    @DisplayName("A Pest created by Pestbrood Sloth gains 1 life when it attacks")
    void pestGainsLifeWhenAttacking() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new PestbroodSloth());
        destroySloth();

        Permanent pest = findPermanents(player1, "Pest").getFirst();
        pest.setSummoningSick(false);
        int pestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pest);

        declareAttackers(player1, List.of(pestIndex));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    private void destroySloth() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
