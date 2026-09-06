package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WishingWell.class, Shock.class, CounselOfTheSoratami.class, GrizzlyBears.class})
class WishingWellTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a coin counter on itself even without a matching graveyard card")
    void putsCounterWithoutMatchingGraveyardCard() {
        Permanent well = harness.addToBattlefieldAndReturn(player1, new WishingWell());
        harness.setGraveyard(player1, List.of(new CounselOfTheSoratami()));

        activateAndResolveAbility();

        assertThat(well.getCounterCount(CounterType.COIN)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Only targets an instant or sorcery whose mana value matches the new coin count")
    void targetsExactManaValue() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(shock, counsel));

        activateAndResolveAbility();
        resolveReflexiveTrigger();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.description()).contains("Shock").doesNotContain("Counsel");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock, counsel);
    }

    @Test
    @DisplayName("Casts the targeted matching instant for free and exiles it after resolution")
    void castsMatchingInstantAndExilesIt() {
        harness.addToBattlefieldAndReturn(player1, new WishingWell());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateAndResolveAbility();
        resolveReflexiveTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(shock.getId()));
    }

    private void activateAndResolveAbility() {
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private void resolveReflexiveTrigger() {
        harness.passBothPriorities();
    }
}
