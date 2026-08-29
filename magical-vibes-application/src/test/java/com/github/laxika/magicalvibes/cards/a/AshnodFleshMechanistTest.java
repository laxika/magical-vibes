package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AshnodFleshMechanistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature for a tapped Powerstone")
    void attackingOffersSacrificeForPowerstone() {
        Permanent ashnod = addCreatureReady(player1, new AshnodFleshMechanist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(ashnod.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger does not sacrifice a creature")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new AshnodFleshMechanist());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("Exiling a creature card creates a tapped 3/3 colorless Zombie artifact token")
    void createsZombieArtifactToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ashnod = harness.addToBattlefieldAndReturn(player1, new AshnodFleshMechanist());
        ashnod.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(ashnod);

        harness.activateAbility(player1, index, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(zombie.getCard().getPower()).isEqualTo(3);
        assertThat(zombie.getCard().getToughness()).isEqualTo(3);
        assertThat(zombie.getCard().getColor()).isNull();
        assertThat(zombie.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(zombie.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(zombie.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activated ability cannot use a noncreature card in the graveyard")
    void activatedAbilityRequiresCreatureCardInGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ashnod = harness.addToBattlefieldAndReturn(player1, new AshnodFleshMechanist());
        ashnod.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ashnod), null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
