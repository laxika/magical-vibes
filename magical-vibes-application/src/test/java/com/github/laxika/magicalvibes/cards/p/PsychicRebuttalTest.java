package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
}
