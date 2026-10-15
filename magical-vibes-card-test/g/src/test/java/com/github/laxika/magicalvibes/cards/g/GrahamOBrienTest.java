package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrahamOBrien.class, GrizzlyBears.class})
class GrahamOBrienTest extends BaseCardTest {

    @Test
    @DisplayName("Paradox creates a Food when casting a spell from exile")
    void paradoxCreatesFoodWhenCastingFromExile() {
        harness.addToBattlefield(player1, new GrahamOBrien());
        Card spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not trigger Paradox")
    void handSpellDoesNotTriggerParadox() {
        harness.addToBattlefield(player1, new GrahamOBrien());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
    }

    @Test
    @DisplayName("Paradox resolves before the spell and survives its source leaving")
    void triggerSurvivesSourceLeaving() {
        var graham = harness.addToBattlefieldAndReturn(player1, new GrahamOBrien());
        Card spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());
        assertThat(findPermanents(player1, "Food")).isEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(graham);
        gd.playerGraveyards.get(player1.getId()).add(graham.getCard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell from exile does not trigger Paradox")
    void opponentsExiledSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new GrahamOBrien());
        Card spell = new GrizzlyBears();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castFromExile(player2, spell.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Created Food can be sacrificed for three life without triggering Paradox")
    void createdFoodCanBeSacrificedForLife() {
        harness.addToBattlefield(player1, new GrahamOBrien());
        Card spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, spell.getId());
        resolveAllTriggers();
        var food = findPermanent(player1, "Food");
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }
}
