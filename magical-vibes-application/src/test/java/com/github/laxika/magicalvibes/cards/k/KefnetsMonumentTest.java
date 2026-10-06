package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HekmaSentinels;
import com.github.laxika.magicalvibes.cards.s.SlitherBlade;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KefnetsMonument.class, AirElemental.class, GrizzlyBears.class, Spellbook.class,
        HekmaSentinels.class, SlitherBlade.class, Cancel.class})
class KefnetsMonumentTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature spells cost {1} less")
    void blueCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        // Air Elemental costs {3}{U}{U} — with the {1} reduction it should cost {2}{U}{U} (4 mana)
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Cannot cast a blue creature short of the reduced cost")
    void cannotCastBlueCreatureWithoutEnoughMana() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        // Reduced cost is {2}{U}{U} (4 mana); 3 mana is not enough
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-blue creature spells are not reduced")
    void nonBlueCreaturesNotReduced() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        // Grizzly Bears costs {1}{G} — not blue, so {G} alone is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Casting a creature locks a chosen opponent creature out of its next untap step")
    void castingCreatureSkipsOpponentUntap() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // The trigger prompts for a "creature an opponent controls" target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger can only target a creature an opponent controls, not your own")
    void triggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // No opponent creature exists, so the trigger has no legal target and is not put on the stack
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(ownCreature.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Casting a non-creature spell does not trigger the skip-untap")
    void nonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        addCreatureReady(player2, new GrizzlyBears()); // a legal target exists
        // Spellbook is a {0} artifact, not a creature spell.
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        harness.setHand(player1, List.of(new SlitherBlade()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Slither Blade");
    }

    @Test
    void opponentCreatureSpellsAreNeitherReducedNorTriggerMonument() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        addCreatureReady(player1, new SlitherBlade());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HekmaSentinels()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Hekma Sentinels");
    }

    @Test
    void blueNoncreatureSpellIsNotReduced() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SlitherBlade()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void triggerDoesNotTapAndExpiresAfterExactlyOneControllerUntapStep() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent target = addCreatureReady(player2, new SlitherBlade());
        harness.setHand(player1, List.of(new SlitherBlade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        target.tap();
        harness.performUntapStep(player1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void repeatedTriggersBeforeUntapDoNotSkipAdditionalUntapSteps() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent target = addCreatureReady(player2, new SlitherBlade());
        target.tap();
        harness.setHand(player1, List.of(new SlitherBlade(), new SlitherBlade()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        for (int i = 0; i < 2; i++) {
            harness.castCreature(player1, 0);
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void castTriggerStillAppliesWhenCreatureSpellIsCountered() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent target = addCreatureReady(player2, new SlitherBlade());
        target.tap();
        SlitherBlade spell = new SlitherBlade();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.assertInGraveyard(player1, "Slither Blade");
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void untapRestrictionExpiresEvenIfTargetWasAlreadyUntapped() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent target = addCreatureReady(player2, new SlitherBlade());
        harness.setHand(player1, List.of(new SlitherBlade()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
        target.tap();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }
}
