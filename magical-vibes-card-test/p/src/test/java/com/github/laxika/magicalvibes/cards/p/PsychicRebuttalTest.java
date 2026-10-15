package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ExquisiteFirecraft;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Guile;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicRebuttal.class, LightningBolt.class, Shock.class, GrizzlyBears.class, ExquisiteFirecraft.class})
class PsychicRebuttalTest extends BaseCardTest {

    private LightningBolt castBoltAt(com.github.laxika.magicalvibes.model.Player target) {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new PsychicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        return bolt;
    }

    @Test
    @DisplayName("Counters an instant that targets you; without spell mastery there is no copy")
    void countersWithoutSpellMastery() {
        LightningBolt bolt = castBoltAt(player2);

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Spell mastery copies the countered spell when the controller accepts")
    void spellMasteryCopiesCounteredSpell() {
        LightningBolt bolt = castBoltAt(player2);
        harness.setGraveyard(player2, List.of(new Shock(), new LightningBolt()));

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);   // copy the spell countered this way
        harness.handleMayAbilityChosen(player2, false);  // keep the copy's target (player2)
        harness.passBothPriorities();                    // the copy resolves

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Spell mastery copy is optional — declining leaves only the counter")
    void spellMasteryCopyCanBeDeclined() {
        LightningBolt bolt = castBoltAt(player2);
        harness.setGraveyard(player2, List.of(new Shock(), new LightningBolt()));

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a spell that targets something other than you")
    void cannotTargetSpellNotTargetingYou() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(new PsychicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bolt.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new PsychicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("The countered owned spell supplies the second card for spell mastery")
    void counteredOwnedSpellEnablesSpellMastery() {
        ExquisiteFirecraft firecraft = new ExquisiteFirecraft();
        harness.setHand(player1, List.of(firecraft, new PsychicRebuttal()));
        harness.setGraveyard(player1, List.of(new PsychicRebuttal()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.castInstant(player1, 0, firecraft.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Exquisite Firecraft");
        harness.assertLife(player1, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An uncounterable sorcery is neither countered nor copied")
    void uncounterableSorceryIsNotCopied() {
        ExquisiteFirecraft firecraft = new ExquisiteFirecraft();
        harness.setHand(player1, List.of(firecraft));
        harness.setGraveyard(player1, List.of(new PsychicRebuttal(), new PsychicRebuttal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new PsychicRebuttal()));
        harness.setGraveyard(player2, List.of(new PsychicRebuttal(), new PsychicRebuttal()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castInstant(player2, 0, firecraft.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player1, "Exquisite Firecraft");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Psychic Rebuttal itself does not supply the second spell mastery card")
    void resolvingRebuttalDoesNotCountForSpellMastery() {
        LightningBolt bolt = castBoltAt(player2);
        harness.setGraveyard(player2, List.of(new PsychicRebuttal()));

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creature cards do not count towards spell mastery")
    void creatureCardsDoNotEnableSpellMastery() {
        LightningBolt bolt = castBoltAt(player2);
        harness.setGraveyard(player2, List.of(new PsychicRebuttal(), new GrizzlyBears()));

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The controller can redirect the copy to the original caster")
    void copyCanChooseNewTarget() {
        LightningBolt bolt = castBoltAt(player2);
        harness.setGraveyard(player2, List.of(new PsychicRebuttal(), new PsychicRebuttal()));

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lightning Bolt");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Guile.class})
    @DisplayName("Guile replacing the counter does not grant a spell mastery copy")
    void replacedCounterDoesNotGrantCopy() {
        LightningBolt bolt = castBoltAt(player2);
        harness.addToBattlefield(player2, new Guile());
        harness.setGraveyard(player2, List.of(new PsychicRebuttal(), new PsychicRebuttal()));

        harness.castInstant(player2, 0, bolt.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bolt.getId()));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }
}
