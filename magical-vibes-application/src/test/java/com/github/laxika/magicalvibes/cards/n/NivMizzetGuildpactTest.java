package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AzoriusSkyguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosCackler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NivMizzetGuildpact.class, AzoriusSkyguard.class, RakdosCackler.class, Forest.class})
class NivMizzetGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage uses each distinct exactly-two-color pair for damage, draw, and life gain")
    void combatDamageUsesDistinctColorPairs() {
        Permanent niv = addCreatureReady(player1, new NivMizzetGuildpact());
        harness.addToBattlefield(player1, new RakdosCackler());
        harness.addToBattlefield(player1, new RakdosCackler());
        harness.addToBattlefield(player1, new AzoriusSkyguard());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        niv.setAttacking(true);
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
