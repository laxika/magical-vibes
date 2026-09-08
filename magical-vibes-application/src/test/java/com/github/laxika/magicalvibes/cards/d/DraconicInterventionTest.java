package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.d.DragonEgg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormentingVoice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraconicIntervention.class, DragonEgg.class, GrizzlyBears.class, TormentingVoice.class})
class DraconicInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the exiled instant or sorcery's mana value")
    void dealsDamageEqualToExiledCardManaValue() {
        TormentingVoice exiledSpell = new TormentingVoice();
        Permanent nonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        DraconicIntervention intervention = new DraconicIntervention();
        harness.setGraveyard(player1, List.of(exiledSpell));
        harness.setHand(player1, List.of(intervention));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, 0);

        StackEntry stackEntry = gd.stack.getFirst();
        assertThat(stackEntry.getXValue()).isEqualTo(2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(nonDragon.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(exiledSpell.getId()))
                .anyMatch(card -> card.getId().equals(intervention.getId()));
    }

    @Test
    @DisplayName("Damages only non-Dragon creatures and exiles creatures killed this turn")
    void sparesDragonsAndExilesKilledCreatures() {
        TormentingVoice exiledSpell = new TormentingVoice();
        Permanent ownNonDragon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new DragonEgg());
        Permanent opponentNonDragon = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        DraconicIntervention intervention = new DraconicIntervention();
        harness.setGraveyard(player1, List.of(exiledSpell));
        harness.setHand(player1, List.of(intervention));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownNonDragon.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(dragon.getId()));
        assertThat(dragon.getMarkedDamage()).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentNonDragon.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(opponentNonDragon.getCard().getId()));
    }
}
