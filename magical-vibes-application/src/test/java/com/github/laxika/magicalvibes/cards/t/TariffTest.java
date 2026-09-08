package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tariff.class, BenalishInfantry.class, BenalishKnight.class, RedwoodTreefolk.class})
class TariffTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the creature's mana cost keeps it")
    void payingKeepsCreature() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castFromHand(player1, new Tariff(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Benalish Infantry");
        harness.assertNotInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("Declining to pay sacrifices the creature")
    void decliningSacrificesCreature() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castFromHand(player1, new Tariff(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("A player who can't pay sacrifices the creature with no prompt")
    void cantPayAutoSacrifices() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.castFromHand(player1, new Tariff(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }

    @Test
    @DisplayName("The creature with the greatest mana value is sacrificed")
    void sacrificesGreatestManaValueCreature() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player1, new RedwoodTreefolk());
        harness.castFromHand(player1, new Tariff(), "{1}{W}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Benalish Infantry");
        harness.assertInGraveyard(player1, "Redwood Treefolk");
    }

    @Test
    @DisplayName("Players with no creatures are unaffected")
    void noCreaturesNeedNoChoice() {
        Tariff tariff = new Tariff();
        harness.castFromHand(player1, tariff, "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(tariff);
    }

    @Test
    @DisplayName("Each player is affected in turn order")
    void eachPlayerAffected() {
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castFromHand(player1, new Tariff(), "{1}{W}");
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.passBothPriorities();

        // The active player is prompted first.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        // The other player is prompted next.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Benalish Infantry");
        harness.assertInGraveyard(player2, "Benalish Knight");
    }

    @Test
    @DisplayName("The active player is affected first even when they joined second")
    void activePlayerIsAffectedFirst() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new BenalishInfantry());
        harness.addToBattlefield(player2, new BenalishKnight());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castFromHand(player2, new Tariff(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Benalish Infantry");
        harness.assertInGraveyard(player2, "Benalish Knight");
    }

    @Test
    @DisplayName("With a tie for greatest mana value, the player chooses which creature is at risk")
    void tieBreakChoosesCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BenalishInfantry());
        harness.castFromHand(player1, new Tariff(), "{1}{W}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, first.getId());

        // The chosen one is sacrificed; the other survives.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .containsExactly(second.getId());
        harness.assertInGraveyard(player1, "Benalish Infantry");
    }
}
