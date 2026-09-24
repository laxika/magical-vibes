package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnsouledScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinBrawler.class, EnsouledScimitar.class})
class GoblinBrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip may target Goblin Brawler but the Equipment does not attach")
    void cannotBeEquipped() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, 1, null, brawler.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("An Equipment already attached to Goblin Brawler becomes unattached")
    void alreadyAttachedEquipmentBecomesUnattached() {
        Permanent brawler = addCreatureReady(player1, new GoblinBrawler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new EnsouledScimitar());
        scimitar.setAttachedTo(brawler.getId());

        harness.runStateBasedActions();

        assertThat(scimitar.getAttachedTo()).isNull();
    }
}
