package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeirdedVampire.class, RavensCrime.class})
class WeirdedVampireTest extends BaseCardTest {

    private WeirdedVampire discardViaRavensCrime() {
        WeirdedVampire vampire = new WeirdedVampire();
        harness.setHand(player1, List.of(vampire));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return vampire;
    }

    @Test
    @DisplayName("Discarding Weirded Vampire exiles it and offers madness cast")
    void discardTriggersMadness() {
        WeirdedVampire vampire = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(vampire.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness cast puts Weirded Vampire into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        WeirdedVampire vampire = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(vampire.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(vampire.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {2}{B} and puts Weirded Vampire onto the battlefield")
    void acceptingMadnessCastsCreature() {
        WeirdedVampire vampire = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(vampire.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Weirded Vampire can be cast normally without being discarded")
    void castsNormallyFromHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WeirdedVampire(), "{3}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Weirded Vampire");
        harness.assertNotInHand(player1, "Weirded Vampire");
        harness.assertNotInGraveyard(player1, "Weirded Vampire");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Madness cannot be paid with three colorless mana and leaves the mana unspent")
    void madnessRequiresBlackMana() {
        WeirdedVampire vampire = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Weirded Vampire");
        harness.assertNotOnBattlefield(player1, "Weirded Vampire");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(vampire.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
