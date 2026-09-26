package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EiganjoDynastorianReplenish.class, Replenish.class, GhostlyPrison.class, GrizzlyBears.class})
class EiganjoDynastorianReplenishTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes prepared when you attack with two creatures")
    void becomesPreparedWithTwoAttackers() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(dynastorian.isPrepared()).isTrue();
        assertThat(dynastorian.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Does not become prepared when you attack with one creature")
    void doesNotBecomePreparedWithOneAttacker() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(dynastorian.isPrepared()).isFalse();
        assertThat(dynastorian.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Casting Replenish returns all enchantment cards and leaves creatures in the graveyard")
    void replenishReturnsEnchantmentsOnly() {
        Permanent dynastorian = addCreatureReady(player1, new EiganjoDynastorianReplenish());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        UUID copyId = dynastorian.getPreparedSpellCardId();
        Card enchantment = new GhostlyPrison();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(enchantment, creature)));

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(dynastorian.isPrepared()).isFalse();
    }

}
