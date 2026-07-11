package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TwinningGlassTest extends BaseCardTest {

    @Test
    @DisplayName("Offers to cast a hand card sharing a name with a spell cast this turn; accepting casts it for free")
    void castsMatchingSpellFromHandForFree() {
        GrizzlyBears prior = new GrizzlyBears();
        GrizzlyBears freeCopy = new GrizzlyBears();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, freeCopy));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0); // cast the first Grizzly Bears
        harness.passBothPriorities();     // resolves onto the battlefield

        harness.activateAbility(player1, 0, null, null); // {1}, {T}
        harness.passBothPriorities();                    // ability resolves

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // cast the copy for free

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(freeCopy.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(freeCopy.getId()));
    }

    @Test
    @DisplayName("Declining the offer leaves the card in hand and casts nothing")
    void decliningDoesNotCast() {
        GrizzlyBears prior = new GrizzlyBears();
        GrizzlyBears freeCopy = new GrizzlyBears();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, freeCopy));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(freeCopy.getId()));
    }

    @Test
    @DisplayName("No offer when no hand card shares a name with a spell cast this turn")
    void noOfferWhenNoHandCardSharesName() {
        GrizzlyBears prior = new GrizzlyBears();
        HillGiant hillGiant = new HillGiant(); // different name — not eligible
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(prior, hillGiant));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0); // cast Grizzly Bears
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(hillGiant.getId()));
    }

    @Test
    @DisplayName("A spell cast by an opponent this turn also enables the ability (any player)")
    void opponentsSpellCounts() {
        LightningBolt boltPrior = new LightningBolt();
        LightningBolt boltFree = new LightningBolt();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(boltFree));
        harness.addMana(player1, ManaColor.COLORLESS, 1); // for the {1} ability cost
        harness.setHand(player2, List.of(boltPrior));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId()); // opponent casts Lightning Bolt
        harness.passBothPriorities();                     // it resolves

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("No offer when no spell was cast this turn")
    void noOfferWhenNoSpellCastThisTurn() {
        GrizzlyBears freeCopy = new GrizzlyBears();
        TwinningGlass glass = new TwinningGlass();

        harness.addToBattlefield(player1, glass);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(freeCopy));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(freeCopy.getId()));
    }
}
