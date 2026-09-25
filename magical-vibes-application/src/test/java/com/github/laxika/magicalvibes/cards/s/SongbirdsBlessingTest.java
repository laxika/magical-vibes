package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SongbirdsBlessing.class, GrizzlyBears.class, HolyStrength.class, Plains.class})
class SongbirdsBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking reveals until an Aura and declining puts it into hand")
    void declinesBattlefieldAndPutsAuraIntoHand() {
        addBlessingAndAttacker();
        Card plains = new Plains();
        Card aura = new HolyStrength();
        harness.setLibrary(player1, List.of(plains, aura));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Holy Strength");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("Accepting puts the revealed Aura onto the battlefield")
    void acceptsBattlefieldPlacement() {
        addBlessingAndAttacker();
        Card plains = new Plains();
        Card aura = new HolyStrength();
        harness.setLibrary(player1, List.of(plains, aura));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Holy Strength");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Plains");
    }

    @Test
    @DisplayName("Without an Aura, all revealed cards return to the bottom")
    void bottomsAllCardsWhenNoAuraIsFound() {
        addBlessingAndAttacker();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(plains, bears));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Plains", "Grizzly Bears");
    }

    private void addBlessingAndAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new SongbirdsBlessing());
        blessing.setAttachedTo(attacker.getId());
    }
}
