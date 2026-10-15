package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VictorManchaRunaway.class, Shock.class, GrizzlyBears.class, Mountain.class})
class VictorManchaRunawayTest extends BaseCardTest {

    private Shock castVictorWithShockInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new VictorManchaRunaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        return shock;
    }

    @Test
    @DisplayName("Exiles a target card from your graveyard and lets you play it while Victor remains controlled")
    void exilesAndAllowsPlayWhileControlled() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = castVictorWithShockInGraveyard();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("Stops allowing the exiled card to be played when Victor leaves the battlefield")
    void permissionEndsWhenVictorLeaves() {
        Shock shock = castVictorWithShockInGraveyard();
        Permanent victor = findPermanent(player1, "Victor Mancha, Runaway");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, victor));

        harness.addMana(player1, ManaColor.RED, 1);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Playing the exiled spell still requires paying its mana cost")
    void requiresManaPayment() {
        Shock shock = castVictorWithShockInGraveyard();

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A creature card can be exiled and cast with its normal mana cost")
    void allowsCreatureCard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new VictorManchaRunaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("A land card can be exiled and played")
    void allowsLandCard() {
        Mountain mountain = new Mountain();
        harness.setGraveyard(player1, List.of(mountain));
        harness.setHand(player1, List.of(new VictorManchaRunaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(mountain.getId()));
        harness.passBothPriorities();

        harness.castFromExile(player1, mountain.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.findExiledCard(mountain.getId())).isNull();
    }

    @Test
    @DisplayName("Victor leaving before resolution still exiles the target but grants no play permission")
    void sourceLeavesBeforeTriggerResolves() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new VictorManchaRunaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        Permanent victor = findPermanent(player1, "Victor Mancha, Runaway");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, victor));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Entering with only an opponent's graveyard populated does not exile their card")
    void cannotTargetOpponentsGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(shock));
        harness.setHand(player1, List.of(new VictorManchaRunaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Victor Mancha, Runaway");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.findExiledCard(shock.getId())).isNull();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
