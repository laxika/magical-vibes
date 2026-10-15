package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({CrystalSpray.class, Plains.class, CrimsonAcolyte.class})
class CrystalSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word and draws a card")
    void changesColorWordAndDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

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

    @Test
    @DisplayName("Replaces the color word in both printed protection and the activated ability")
    void changesEveryColorWordInPrintedAbilities() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CrimsonAcolyte());
        Permanent recipient = harness.addToBattlefieldAndReturn(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, source.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, source, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, source, CardColor.RED)).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasProtectionFrom(gd, recipient, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("Changing a basic land type changes the land's intrinsic mana ability")
    void changedPlainsProducesBlueMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());
        harness.handleListChoice(player1, "PLAINS");
        harness.handleListChoice(player1, "ISLAND");
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("A word absent from the target can be chosen and the card is still drawn")
    void absentWordStillDrawsCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CrimsonAcolyte());
        harness.setHand(player1, List.of(new CrystalSpray()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isFalse();
        harness.assertInHand(player1, "Plains");
    }
}
