package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NoDachi;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBrawler.class, NoDachi.class})
class GoblinBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip may target Goblin Brawler but the Equipment does not attach")
    void cannotBeEquipped() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent noDachi = harness.addToBattlefieldAndReturn(player1, new NoDachi());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 1, null, brawler.getId());
        harness.passBothPriorities();

        assertThat(noDachi.getAttachedTo()).isNull();
    }
}
