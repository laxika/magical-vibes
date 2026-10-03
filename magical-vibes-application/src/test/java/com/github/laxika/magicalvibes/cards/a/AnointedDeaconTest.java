package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DuskborneSkymarcher;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnointedDeacon.class, DuskborneSkymarcher.class, ColossalDreadmaw.class})
class AnointedDeaconTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    @Test
    @DisplayName("Accepting the may ability and targeting a Vampire gives it +2/+0")
    void acceptAndTargetVampireGivesBoost() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new DuskborneSkymarcher());
        UUID vampireId = harness.getPermanentId(player1, "Duskborne Skymarcher");

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, vampireId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent vampire = findPermanent(player1, "Duskborne Skymarcher");
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target itself since it is a Vampire")
    void canTargetItself() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        UUID deaconId = harness.getPermanentId(player1, "Anointed Deacon");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, deaconId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent deacon = findPermanent(player1, "Anointed Deacon");
        assertThat(deacon.getPowerModifier()).isEqualTo(2);
        assertThat(deacon.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the may ability does not boost any creature")
    void declineMayAbilityNoBoost() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new DuskborneSkymarcher());
        UUID vampireId = harness.getPermanentId(player1, "Duskborne Skymarcher");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, vampireId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent vampire = findPermanent(player1, "Duskborne Skymarcher");
        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not trigger during opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new DuskborneSkymarcher());

        advanceToCombat(player2); // opponent's combat
        harness.passBothPriorities();

        // No may prompt — stack should be empty and no interaction awaiting
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target opponent's Vampire")
    void canTargetOpponentVampire() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player2, new DuskborneSkymarcher());
        UUID vampireId = harness.getPermanentId(player2, "Duskborne Skymarcher");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, vampireId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent vampire = findPermanent(player2, "Duskborne Skymarcher");
        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new DuskborneSkymarcher());
        UUID vampireId = harness.getPermanentId(player1, "Duskborne Skymarcher");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, vampireId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent vampire = findPermanent(player1, "Duskborne Skymarcher");
        assertThat(vampire.getPowerModifier()).isEqualTo(2);

        // Advance to end step — modifiers reset
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(vampire.getPowerModifier()).isEqualTo(0);
        assertThat(vampire.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Only Vampires are offered as targets")
    void nonVampireIsNotALegalTarget() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        UUID deaconId = harness.getPermanentId(player1, "Anointed Deacon");

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(deaconId);
        harness.handlePermanentChosen(player1, deaconId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("The boost resolves even if Anointed Deacon leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new DuskborneSkymarcher());
        Permanent deacon = findPermanent(player1, "Anointed Deacon");
        Permanent vampire = findPermanent(player1, "Duskborne Skymarcher");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, vampire.getId());
        gd.playerBattlefields.get(player1.getId()).remove(deacon);
        gd.playerGraveyards.get(player1.getId()).add(deacon.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(vampire.getPowerModifier()).isEqualTo(2);
        assertThat(vampire.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A removed target prevents resolution and the optional boost choice")
    void removedTargetPreventsResolution() {
        harness.addToBattlefield(player1, new AnointedDeacon());
        harness.addToBattlefield(player1, new DuskborneSkymarcher());
        Permanent vampire = findPermanent(player1, "Duskborne Skymarcher");

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, vampire.getId());
        gd.playerBattlefields.get(player1.getId()).remove(vampire);
        gd.playerGraveyards.get(player1.getId()).add(vampire.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(vampire.getPowerModifier()).isZero();
        assertThat(findPermanent(player1, "Anointed Deacon").getPowerModifier()).isZero();
    }
}
