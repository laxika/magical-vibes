package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Oakenform;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaevictisAsmadiTheDire.class, GrizzlyBears.class, AirElemental.class, Shock.class, Oakenform.class})
class VaevictisAsmadiTheDireTest extends BaseCardTest {

    @Test
    void sacrificesOnePermanentPerPlayerAndPutsPermanentTopCardsOntoTheBattlefield() {
        Permanent vaevictis = addCreatureReady(player1, new VaevictisAsmadiTheDire());
        Permanent ownTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new AirElemental()));
        Card nonPermanent = new Shock();
        harness.setLibrary(player2, List.of(nonPermanent));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownTarget.getId(), opponentTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownTarget.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Air Elemental"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentTarget.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(nonPermanent);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vaevictis);
    }

    @Test
    void cannotOmitOwnTargetWhenBothPlayersControlPermanents() {
        addCreatureReady(player1, new VaevictisAsmadiTheDire());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(opponentTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotChooseZeroTargetsWhenBothPlayersControlPermanents() {
        addCreatureReady(player1, new VaevictisAsmadiTheDire());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void triggerIsRemovedWhenAPlayerControlsNoPermanents() {
        Permanent vaevictis = addCreatureReady(player1, new VaevictisAsmadiTheDire());
        Card topCard = new AirElemental();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(vaevictis);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void canSacrificeVaevictisItselfAndResolveWithEmptyLibraries() {
        Permanent vaevictis = addCreatureReady(player1, new VaevictisAsmadiTheDire());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(vaevictis.getId(), opponentTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vaevictis.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentTarget.getCard());
    }

    @Test
    void revealedAuraStaysInLibraryWhenNothingCanBeEnchanted() {
        Permanent vaevictis = addCreatureReady(player1, new VaevictisAsmadiTheDire());
        Permanent opponentTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card aura = new Oakenform();
        harness.setLibrary(player1, List.of(aura));
        harness.setLibrary(player2, List.of(new Shock()));

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(vaevictis.getId(), opponentTarget.getId()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
    }
}
