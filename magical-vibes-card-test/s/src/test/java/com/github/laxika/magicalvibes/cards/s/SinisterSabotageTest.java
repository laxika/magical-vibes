package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InescapableBlaze;
import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinisterSabotage.class, VernadiShieldmate.class, InescapableBlaze.class})
class SinisterSabotageTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell and surveils 1")
    void countersSpellAndSurveilsOne() {
        VernadiShieldmate creature = new VernadiShieldmate();
        Card topCard = new VernadiShieldmate();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        harness.assertInGraveyard(player2, "Sinister Sabotage");
    }

    @Test
    @DisplayName("Leaves the top card on the library when surveil is declined")
    void declinesSurveil() {
        VernadiShieldmate creature = new VernadiShieldmate();
        Card topCard = new VernadiShieldmate();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(topCard));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
        harness.assertInGraveyard(player2, "Sinister Sabotage");
    }

    @Test
    @DisplayName("Counters a spell even when the surveilling player's library is empty")
    void countersWithEmptyLibrary() {
        VernadiShieldmate creature = new VernadiShieldmate();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        harness.assertInGraveyard(player2, "Sinister Sabotage");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not surveil when the targeted spell has already left the stack")
    void doesNotSurveilWhenTargetIsGone() {
        VernadiShieldmate creature = new VernadiShieldmate();
        Card topCard = new VernadiShieldmate();
        harness.setHand(player1, List.of(creature, new SinisterSabotage()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of());
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vernadi Shieldmate");
        harness.assertInGraveyard(player1, "Sinister Sabotage");
        harness.assertInGraveyard(player2, "Sinister Sabotage");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveils even when the targeted spell cannot be countered")
    void surveilsAgainstUncounterableSpell() {
        InescapableBlaze blaze = new InescapableBlaze();
        Card topCard = new VernadiShieldmate();
        harness.setHand(player1, List.of(blaze));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLibrary(player2, List.of(topCard));

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, blaze.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        harness.assertInGraveyard(player2, "Sinister Sabotage");
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Inescapable Blaze");
        assertThat(gd.stack).isEmpty();
    }
}
