package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FathomFleetCutthroat.class, LoomingAltisaur.class, LightningStrike.class})
class FathomFleetCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys target creature an opponent controls that was dealt damage this turn")
    void etbDestroysCreatureDealtDamageThisTurn() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur()).getId();

        // Mark the creature as having been dealt damage this turn
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);

        // Resolve ETB → destroys the target
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Looming Altisaur");
        harness.assertInGraveyard(player2, "Looming Altisaur");
    }

    @Test
    @DisplayName("Cannot target creature that was not dealt damage this turn")
    void cannotTargetCreatureNotDealtDamage() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur()).getId();

        // Do NOT mark the creature as dealt damage

        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target own creature even if it was dealt damage this turn")
    void cannotTargetOwnCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new LoomingAltisaur()).getId();

        // Mark own creature as dealt damage
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Can cast without target when no creature was dealt damage this turn")
    void canCastWithoutTargetWhenNoCreatureDealtDamage() {
        harness.addToBattlefield(player2, new LoomingAltisaur());
        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fathom Fleet Cutthroat");
    }

    @Test
    @DisplayName("ETB leaves no ability on the stack when no legal target exists")
    void etbLeavesNoAbilityOnStackWithoutLegalTarget() {
        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fathom Fleet Cutthroat");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Indestructible creature survives the ETB")
    void indestructibleCreatureSurvives() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur()).getId();
        gd.permanentsDealtDamageThisTurn.add(targetId);

        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, targetId);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Grant indestructible before ETB resolves
        Permanent target = findPermanent(player2, "Looming Altisaur");
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB → creature survives
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Looming Altisaur");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("indestructible"));
    }

    @Test
    @DisplayName("ETB destroys a creature damaged by Lightning Strike earlier this turn")
    void destroysCreatureAfterActualSpellDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        harness.setHand(player1, List.of(new LightningStrike(), new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.assertOnBattlefield(player2, "Looming Altisaur");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Looming Altisaur");
        harness.assertNotOnBattlefield(player2, "Looming Altisaur");
        harness.assertOnBattlefield(player1, "Fathom Fleet Cutthroat");
    }

    @Test
    @DisplayName("A creature damaged in response to Cutthroat becomes a legal ETB target")
    void choosesTargetDamagedWhileCreatureSpellIsOnStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        harness.setHand(player1, List.of(new FathomFleetCutthroat(), new LightningStrike()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Looming Altisaur");
        harness.assertNotOnBattlefield(player2, "Looming Altisaur");
        harness.assertOnBattlefield(player1, "Fathom Fleet Cutthroat");
    }

    @Test
    @DisplayName("ETB does not destroy its target after that creature changes to your control")
    void targetBecomesIllegalWhenItsControllerChanges() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LoomingAltisaur());
        gd.permanentsDealtDamageThisTurn.add(target.getId());
        harness.setHand(player1, List.of(new FathomFleetCutthroat()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Looming Altisaur");
        harness.assertNotInGraveyard(player2, "Looming Altisaur");
    }
}
