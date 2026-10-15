package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SphinxOfJwarIsle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DocOcksTentacles.class, GrizzlyBears.class, HillGiant.class, SerraAngel.class, SphinxOfJwarIsle.class})
class DocOcksTentaclesTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+4")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent tentacles = addTentaclesReady(player1);
        tentacles.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Accepting the may attaches Doc Ock's Tentacles to a creature with mana value 5 or greater")
    void attachesToEnteringExpensiveCreatureOnAccept() {
        Permanent tentacles = addTentaclesReady(player1);
        castSerraAngel(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        Permanent angel = findPermanent(player1, "Serra Angel");
        assertThat(tentacles.getAttachedTo()).isEqualTo(angel.getId());
    }

    @Test
    @DisplayName("Declining the may leaves Doc Ock's Tentacles unattached")
    void staysUnattachedOnDecline() {
        Permanent tentacles = addTentaclesReady(player1);
        castSerraAngel(player1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(tentacles.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for a creature with mana value less than 5")
    void doesNotTriggerForCheapCreature() {
        Permanent tentacles = addTentaclesReady(player1);

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(tentacles.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Does not trigger for an expensive creature an opponent controls")
    void doesNotTriggerForOpponentsCreature() {
        Permanent tentacles = addTentaclesReady(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castSerraAngel(player2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(tentacles.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Resolving equip attaches Doc Ock's Tentacles to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent tentacles = addTentaclesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(tentacles.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The triggered attachment can attach to an entering creature with shroud")
    void attachesToEnteringCreatureWithShroud() {
        Permanent tentacles = addTentaclesReady(player1);
        harness.setHand(player1, List.of(new SphinxOfJwarIsle()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent sphinx = findPermanent(player1, "Sphinx of Jwar Isle");
        assertThat(tentacles.getAttachedTo()).isEqualTo(sphinx.getId());
        assertThat(gqs.getEffectivePower(gd, sphinx)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, sphinx)).isEqualTo(9);
    }

    @Test
    @DisplayName("Accepting moves the Equipment from its previous creature to the entering creature")
    void acceptingMovesEquipmentAndBoost() {
        Permanent previous = addCreatureReady(player1, new GrizzlyBears());
        Permanent tentacles = addTentaclesReady(player1);
        tentacles.setAttachedTo(previous.getId());
        castSerraAngel(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent angel = findPermanent(player1, "Serra Angel");
        assertThat(tentacles.getAttachedTo()).isEqualTo(angel.getId());
        assertThat(gqs.getEffectivePower(gd, previous)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(8);
    }

    @Test
    @DisplayName("Declining preserves the Equipment's previous attachment")
    void decliningPreservesPreviousAttachment() {
        Permanent previous = addCreatureReady(player1, new GrizzlyBears());
        Permanent tentacles = addTentaclesReady(player1);
        tentacles.setAttachedTo(previous.getId());
        castSerraAngel(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent angel = findPermanent(player1, "Serra Angel");
        assertThat(tentacles.getAttachedTo()).isEqualTo(previous.getId());
        assertThat(gqs.getEffectivePower(gd, previous)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, previous)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(4);
    }

    private Permanent addTentaclesReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DocOcksTentacles());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void castSerraAngel(Player player) {
        harness.setHand(player, List.of(new SerraAngel()));
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castCreature(player, 0);
    }
}
