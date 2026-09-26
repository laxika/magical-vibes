package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystalSpray.class, CrimsonAcolyte.class, Plains.class})
class CrystalSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word and draws a card")
    void changesColorWordAndDrawsCard() {
        harness.addToBattlefield(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Crimson Acolyte");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        Permanent target = findPermanent(player2, "Crimson Acolyte");
        assertThat(target.getTextReplacements()).containsExactly(new TextReplacement("red", "green", true));
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Changes a basic land type word on a target permanent")
    void changesBasicLandTypeWord() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Plains");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "PLAINS");
        harness.handleListChoice(player1, "ISLAND");

        assertThat(findPermanent(player2, "Plains").getTextReplacements())
                .containsExactly(new TextReplacement("Plains", "Island", true));
    }

    @Test
    @DisplayName("Carries a text change from a target spell onto the permanent it becomes")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new CrystalSpray(), new CrimsonAcolyte()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 1);
        UUID creatureSpellId = gd.stack.getFirst().getCard().getId();
        harness.castAndResolveInstant(player1, 0, creatureSpellId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Crimson Acolyte").getTextReplacements())
                .containsExactly(new TextReplacement("red", "green", true));
    }

    @Test
    @DisplayName("The text change wears off at end of turn")
    void textChangeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Crimson Acolyte");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        Permanent target = findPermanent(player2, "Crimson Acolyte");
        assertThat(target.getTextReplacements()).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getTextReplacements()).isEmpty();
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Crimson Acolyte");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }
}
