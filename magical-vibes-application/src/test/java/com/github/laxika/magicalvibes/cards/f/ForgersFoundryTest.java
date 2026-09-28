package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HighTide;
import com.github.laxika.magicalvibes.cards.t.Tidings;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgersFoundry.class, HighTide.class, Tidings.class})
class ForgersFoundryTest extends BaseCardTest {

    @Test
    void exilesEligibleSpellWhenAccepted() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        HighTide highTide = new HighTide();
        harness.setHand(player1, List.of(highTide));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);

        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(foundry.getId())).contains(highTide);
        harness.assertNotInGraveyard(player1, "High Tide");
    }

    @Test
    void doesNotTriggerForSpellWithManaValueAboveThree() {
        harness.addToBattlefield(player1, new ForgersFoundry());
        harness.setHand(player1, List.of(new Tidings()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Tidings");
    }

    @Test
    void secondAbilityGrantsFreeCastPermissionForCardsExiledWithFoundry() {
        Permanent foundry = harness.addToBattlefieldAndReturn(player1, new ForgersFoundry());
        HighTide highTide = new HighTide();
        gd.addToExile(player1.getId(), highTide, foundry.getId());
        foundry.untap();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.exileCastPermissionsUntilEndOfTurn).anyMatch(permission ->
                permission.cardId().equals(highTide.getId())
                        && permission.withoutPayingManaCost());
    }
}
