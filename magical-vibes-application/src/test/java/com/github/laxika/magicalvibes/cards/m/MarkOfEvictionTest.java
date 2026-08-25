package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.e.EssenceFlare;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkOfEviction.class, GrizzlyBears.class, ArmorOfFaith.class, EssenceFlare.class})
class MarkOfEvictionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns the enchanted creature and all attached Auras to their owners' hands")
    void returnsEnchantedCreatureAndAllAttachedAuras() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mark = addAttachedAura(player1, new MarkOfEviction(), creature);
        Permanent ownAura = addAttachedAura(player1, new ArmorOfFaith(), creature);
        Permanent opponentAura = addAttachedAura(player2, new EssenceFlare(), creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(mark.getCard(), ownAura.getCard());
        assertThat(gd.playerHands.get(player2.getId()))
                .contains(creature.getCard(), opponentAura.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(mark, ownAura);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(creature, opponentAura);
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringItsControllersUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent mark = addAttachedAura(player1, new MarkOfEviction(), creature);

        advanceToUpkeep(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mark);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    private Permanent addAttachedAura(Player controller, Card auraCard, Permanent creature) {
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
