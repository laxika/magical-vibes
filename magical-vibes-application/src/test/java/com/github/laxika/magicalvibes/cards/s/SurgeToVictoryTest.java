package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgeToVictory.class, DarkRitual.class, GrizzlyBears.class, Shock.class})
class SurgeToVictoryTest extends BaseCardTest {

    @Test
    void exilesTheTargetAndBoostsOwnCreaturesByItsManaValue() {
        DarkRitual ritual = new DarkRitual();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(ritual));
        castSurge(ritual);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ritual);
    }

    @Test
    void combatDamageOffersAFreeCopyOfTheExiledCard() {
        Shock shock = new Shock();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(shock));
        castSurge(shock);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    private void castSurge(Card targetCard) {
        harness.setHand(player1, List.of(new SurgeToVictory()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castSorcery(player1, 0, List.of(targetCard.getId()));
        harness.passBothPriorities();
    }
}
