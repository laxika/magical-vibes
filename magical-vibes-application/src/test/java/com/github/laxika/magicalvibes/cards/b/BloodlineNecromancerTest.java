package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodlineNecromancer.class, BloodthroneVampire.class, FugitiveWizard.class, GrizzlyBears.class})
class BloodlineNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets a Vampire or Wizard creature card and returns it to the battlefield")
    void returnsTargetVampireOrWizardToBattlefield() {
        Card vampire = new BloodthroneVampire();
        Card wizard = new FugitiveWizard();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(vampire, wizard, bears));

        castBloodlineNecromancer();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(vampire.getId(), wizard.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");

        harness.handleMultipleCardsChosen(player1, List.of(wizard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Bloodthrone Vampire");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional ETB return leaves the target in the graveyard")
    void decliningReturnLeavesTargetInGraveyard() {
        Card vampire = new BloodthroneVampire();
        harness.setGraveyard(player1, List.of(vampire));

        castBloodlineNecromancer();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(vampire.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Bloodline Necromancer");
        harness.assertInGraveyard(player1, "Bloodthrone Vampire");
    }

    @Test
    @DisplayName("ETB does not target a non-Vampire or non-Wizard creature")
    void doesNotTargetOtherCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castBloodlineNecromancer();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB returns a Vampire without requiring it to also be a Wizard")
    void returnsVampireToBattlefield() {
        Card vampire = new BloodthroneVampire();
        harness.setGraveyard(player1, List.of(vampire));

        castBloodlineNecromancer();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(vampire.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Bloodthrone Vampire");
        harness.assertNotInGraveyard(player1, "Bloodthrone Vampire");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(vampire.getId()))
                .singleElement().satisfies(permanent -> assertThat(permanent.isTapped()).isFalse());
    }

    @Test
    @DisplayName("ETB cannot target a qualifying creature in an opponent's graveyard")
    void onlyTargetsControllersGraveyard() {
        Card ownWizard = new FugitiveWizard();
        Card opposingVampire = new BloodthroneVampire();
        harness.setGraveyard(player1, List.of(ownWizard));
        harness.setGraveyard(player2, List.of(opposingVampire));

        castBloodlineNecromancer();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownWizard.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingVampire.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");

        harness.handleMultipleCardsChosen(player1, List.of(ownWizard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Bloodthrone Vampire");
        harness.assertNotOnBattlefield(player1, "Bloodthrone Vampire");
    }

    @Test
    @DisplayName("ETB with an empty graveyard does not request a target or an optional return")
    void emptyGraveyardNeedsNoChoice() {
        harness.setGraveyard(player1, List.of());

        castBloodlineNecromancer();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bloodline Necromancer");
    }

    @Test
    @DisplayName("A target that leaves the graveyard is not replaced by another eligible card")
    void removedTargetDoesNotReturnAnotherCard() {
        Card vampire = new BloodthroneVampire();
        Card wizard = new FugitiveWizard();
        harness.setGraveyard(player1, List.of(vampire, wizard));

        castBloodlineNecromancer();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(vampire.getId()));
        harness.setGraveyard(player1, List.of(wizard));
        harness.setHand(player1, List.of(vampire));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Bloodthrone Vampire");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertNotOnBattlefield(player1, "Bloodthrone Vampire");
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Lifelink gains life for combat damage dealt by Bloodline Necromancer")
    void combatDamageGainsLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent necromancer = addCreatureReady(player1, new BloodlineNecromancer());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(necromancer)));
        resolveCombat();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private void castBloodlineNecromancer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new BloodlineNecromancer(), "{4}{B}");
    }
}
