package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.Expropriate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivinerOfMist.class, Shock.class, Expropriate.class, GrizzlyBears.class})
class DivinerOfMistTest extends BaseCardTest {

    @Test
    void attackMillsFourAndOffersOnlyEligibleGraveyardSpell() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock, new Expropriate(), new GrizzlyBears()));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn.get(shock.getId())).isNotNull();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn.get(shock.getId()).withoutPayingManaCost())
                .isTrue();
        assertThat(gd.graveyardCardCastPermissionsUntilEndOfTurn)
                .hasSize(1)
                .containsKey(shock.getId());
    }

    @Test
    void eligibleSpellCanBeCastForFreeAndIsExiledAfterResolving() {
        Shock shock = new Shock();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(shock));
        Permanent diviner = addCreatureReady(player1, new DivinerOfMist());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(diviner)));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }
}
