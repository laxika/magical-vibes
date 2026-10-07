package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({TraitDoctoring.class, PaladinEnVec.class, GrizzlyBears.class, DragonsClaw.class, Swamp.class})
class TraitDoctoringTest extends BaseCardTest {

    @Test
    @DisplayName("Replaces a color word on the target permanent when cipher is declined")
    void replacesColorWord() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

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

        harness.castAndResolveSorcery(player1, 0, target.getId());

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

        harness.castAndResolveSorcery(player1, 0, target.getId());
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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
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

    @Test
    @DisplayName("Changed protection applies until cleanup and then the original protection returns")
    void changedProtectionExpiresAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.GREEN)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Changing a basic land type changes its mana ability until cleanup")
    void changedLandTypeAndManaExpireAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Swamp());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleListChoice(player1, "SWAMP");
        harness.handleListChoice(player1, "FOREST");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.effectiveBasicLandTypes(gd, target)).containsExactly(CardSubtype.FOREST);
        harness.tapPermanent(player2, 0);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, target)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Changing red to green changes which spells trigger Dragon's Claw")
    void changesColorWordInPrintedTrigger() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.handleMayAbilityChosen(player1, false);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard() instanceof DragonsClaw);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal target prevents both the text change and encoding")
    void illegalTargetPreventsEncoding() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Trait Doctoring");
    }

    @Test
    @DisplayName("Cipher can encode on the target even after it gains protection from blue")
    void encodingDoesNotTargetTheCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PaladinEnVec());
        harness.setHand(player1, List.of(new TraitDoctoring()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "BLUE");
        assertThat(gqs.hasProtectionFrom(gd, target, CardColor.BLUE)).isTrue();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Trait Doctoring"));
        harness.assertNotInGraveyard(player1, "Trait Doctoring");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
