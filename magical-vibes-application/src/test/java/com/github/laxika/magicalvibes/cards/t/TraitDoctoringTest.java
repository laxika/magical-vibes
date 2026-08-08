package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TraitDoctoringTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a color word on the target permanent when cipher is declined")
    void replacesColorWord() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("black", "green", true));
        harness.assertInGraveyard(player1, "Trait Doctoring");
    }

    @Test
    @DisplayName("Replaces a basic land type on the target permanent")
    void replacesLandType() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(target.getTextReplacements())
                .containsExactly(new TextReplacement("Swamp", "Forest", true));
    }

    @Test
    @DisplayName("The text change wears off at end of turn")
    void textChangeWearsOff() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, false);
        assertThat(target.getTextReplacements()).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getTextReplacements()).isEmpty();
    }

    @Test
    @DisplayName("Encodes on a creature and casts a copy after combat damage")
    void encodesAndCastsCopy() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, attacker.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Trait Doctoring"));
        harness.assertNotInGraveyard(player1, "Trait Doctoring");

        target.tap();
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        harness.handleListChoice(player1, "RED");

        assertThat(target.getTextReplacements())
                .contains(new TextReplacement("white", "red", true));
        assertThat(gd.exiledCards).hasSize(1);
    }
}
