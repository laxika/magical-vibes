package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisplacerKitten.class, GrizzlyBears.class, Island.class, Shock.class})
class DisplacerKittenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell flickers a target nonland permanent you control")
    void noncreatureSpellFlickersTarget() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("The trigger can resolve without a target")
    void canChooseNoTarget() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).singleElement()
                .extracting(entry -> entry.getEntryType())
                .isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The trigger cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        castNoncreatureSpell();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trigger cannot target an opponent's permanent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNoncreatureSpell();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The Kitten can flicker itself before the triggering spell resolves")
    void canFlickerItself() {
        Permanent kitten = harness.addToBattlefieldAndReturn(player1, new DisplacerKitten());
        castNoncreatureSpell();

        harness.handlePermanentChosen(player1, kitten.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Displacer Kitten");
        assertThat(harness.getPermanentId(player1, "Displacer Kitten")).isNotEqualTo(kitten.getId());
        assertThat(gd.stack).singleElement()
                .extracting(entry -> entry.getEntryType())
                .isEqualTo(StackEntryType.INSTANT_SPELL);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A stolen permanent returns under its owner's control")
    void stolenPermanentReturnsToOwner() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        GrizzlyBears bears = new GrizzlyBears();
        bears.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, bears);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Borrowed creature", null,
                player1.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                target.getId(), null, null, EffectDuration.PERMANENT, 0));
        castNoncreatureSpell();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger the Kitten")
    void opponentNoncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).singleElement()
                .extracting(entry -> entry.getEntryType())
                .isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("A target that changes controllers before resolution is not flickered")
    void targetMustRemainUnderYourControl() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control change", null,
                player2.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                target.getId(), null, null, EffectDuration.PERMANENT, 0));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isEqualTo(target.getId());
    }
    @Test
    @DisplayName("The trigger still resolves after the Kitten is destroyed")
    void triggerSurvivesSourceRemoval() {
        Permanent kitten = harness.addToBattlefieldAndReturn(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, kitten.getId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Displacer Kitten");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("A destroyed target is not returned from the graveyard")
    void destroyedTargetIsNotReturned() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();
        harness.handlePermanentChosen(player1, target.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, target.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
    private void castNoncreatureSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
