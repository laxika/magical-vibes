package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GildedAmbusher.class, GrizzlyBears.class, Shock.class, Forest.class})
class GildedAmbusherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice on attack exiles an opposing permanent and random nonland card, then deals their total mana value")
    void sacrificesAndExilesBeforeDealingDamage() {
        Permanent ambusher = addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        Forest forest = new Forest();
        Shock exiledFromLibrary = new Shock();
        harness.setLibrary(player2, List.of(forest, exiledFromLibrary));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, opposing.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ambusher).doesNotContain(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposing);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiledFromLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(forest);
        assertThat(player2.getLifeTotal()).isEqualTo(13);
    }

    @Test
    @DisplayName("Declining the attack trigger does nothing")
    void mayBeDeclined() {
        Permanent ambusher = addCreatureReady(player1, new GildedAmbusher());
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposing = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ambusher, sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposing);
        assertThat(player2.getLifeTotal()).isEqualTo(16);
    }
}
