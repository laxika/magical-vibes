package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.Afflict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedMonstrosity.class, Afflict.class, Forest.class, Chainflinger.class})
class CursedMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when targeted by a spell without a land to discard")
    void sacrificesWhenTargetedBySpellWithoutLandToDiscard() {
        Permanent monstrosity = addMonstrosityWithHand(new Afflict());

        targetWithAfflict(monstrosity);

        harness.assertNotOnBattlefield(player1, "Cursed Monstrosity");
        harness.assertInGraveyard(player1, "Cursed Monstrosity");
        harness.assertInHand(player1, "Afflict");
    }

    @Test
    @DisplayName("Discards a land instead of sacrificing itself when targeted")
    void discardsLandInsteadOfSacrificingWhenTargeted() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new CursedMonstrosity());
        harness.setHand(player1, List.of(new Forest()));

        targetWithAfflict(monstrosity);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cursed Monstrosity");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices itself when the controller declines to discard a land")
    void sacrificesWhenDiscardIsDeclined() {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new CursedMonstrosity());
        harness.setHand(player1, List.of(new Forest()));

        targetWithAfflict(monstrosity);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Cursed Monstrosity");
        harness.assertInGraveyard(player1, "Cursed Monstrosity");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Sacrifices itself when targeted by an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent monstrosity = addMonstrosityWithHand(new Afflict());

        Permanent chainflinger = addCreatureReady(player2, new Chainflinger());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(chainflinger),
                null, monstrosity.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cursed Monstrosity");
        harness.assertInGraveyard(player1, "Cursed Monstrosity");
    }

    private void targetWithAfflict(Permanent monstrosity) {
        harness.setHand(player2, List.of(new Afflict()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, monstrosity.getId());
        harness.passBothPriorities();
    }

    private Permanent addMonstrosityWithHand(Card handCard) {
        Permanent monstrosity = harness.addToBattlefieldAndReturn(player1, new CursedMonstrosity());
        harness.setHand(player1, List.of(handCard));
        return monstrosity;
    }
}
