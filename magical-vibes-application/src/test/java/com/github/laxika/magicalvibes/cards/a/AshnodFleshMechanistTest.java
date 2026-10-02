package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({AshnodFleshMechanist.class, ArgothianSprite.class, Swamp.class})
class AshnodFleshMechanistTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature for a tapped Powerstone")
    void attackingOffersSacrificeForPowerstone() {
        Permanent ashnod = addCreatureReady(player1, new AshnodFleshMechanist());
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(sprite.getId());
        assertThat(choice.validIds()).doesNotContain(ashnod.getId());

        harness.handlePermanentChosen(player1, sprite.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sprite.getCard());
        Permanent powerstone = findPermanent(player1, "Powerstone");
        assertThat(powerstone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining the attack trigger does not sacrifice a creature")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new AshnodFleshMechanist());
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sprite);
        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("Exiling a creature card creates a tapped 3/3 colorless Zombie artifact token")
    void createsZombieArtifactToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ashnod = addCreatureReady(player1, new AshnodFleshMechanist());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setGraveyard(player1, List.of(new ArgothianSprite()));
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
        Permanent ashnod = addCreatureReady(player1, new AshnodFleshMechanist());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setGraveyard(player1, List.of(new Swamp()));

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(ashnod), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Powerstone is created during the original attack trigger resolution")
    void powerstoneCreationDoesNotUseAnotherTrigger() {
        addCreatureReady(player1, new AshnodFleshMechanist());
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.withAutoStop(gd.currentStep,
                () -> harness.handlePermanentChosen(player1, sprite.getId()));

        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice choices exclude opposing creatures and noncreature permanents")
    void onlyAnotherControlledCreatureCanBeSacrificed() {
        addCreatureReady(player1, new AshnodFleshMechanist());
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        harness.addToBattlefield(player1, new Swamp());
        addCreatureReady(player2, new ArgothianSprite());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(sprite.getId());
        harness.handlePermanentChosen(player1, sprite.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player2, "Powerstone")).isEmpty();
    }

    @Test
    @DisplayName("A tapped summoning-sick Ashnod can activate and pays exile before resolution")
    void activationPaysCostsBeforeCreatingToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent ashnod = harness.addToBattlefieldAndReturn(player1, new AshnodFleshMechanist());
        ashnod.setSummoningSick(true);
        ashnod.setTapped(true);
        ArgothianSprite creature = new ArgothianSprite();
        Swamp land = new Swamp();
        harness.setGraveyard(player1, List.of(land, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN,
                () -> harness.handleGraveyardCardChosen(player1, 1));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(creature));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("The exile cost cannot use an opponent's creature card")
    void cannotExileOpponentsGraveyardCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AshnodFleshMechanist());
        harness.setGraveyard(player2, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("The activated ability requires five mana")
    void activationRequiresFiveMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new AshnodFleshMechanist());
        ArgothianSprite creature = new ArgothianSprite();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }
}
