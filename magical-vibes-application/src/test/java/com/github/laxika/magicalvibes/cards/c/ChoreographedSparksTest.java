package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChoreographedSparks.class, AngelsMercy.class, LightningBolt.class, RuneclawBear.class, Twincast.class})
class ChoreographedSparksTest extends BaseCardTest {

    // Generous colorless covers generic costs; red is over-provisioned so Choreographed Sparks' {R}{R}
    // is never starved by an earlier spell's generic cost being paid with red.
    private void giveSparksMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 5);
        harness.addMana(player, ManaColor.COLORLESS, 4);
    }


    @Test
    @DisplayName("Mode 0 copies a target instant/sorcery spell you control")
    void mode0CopiesInstantYouControl() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new ChoreographedSparks()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        giveSparksMana(player1);

        harness.castInstant(player1, 0); // Angel's Mercy on the stack
        harness.castInstant(player1, 0, 0, mercy.getId()); // Choreographed Sparks, mode 0

        // Resolve Choreographed Sparks → creates a copy of Angel's Mercy.
        harness.passBothPriorities();

        StackEntry copy = gd.stack.getLast();
        assertThat(copy.getDescription()).isEqualTo("Copy of Angel's Mercy");
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getControllerId()).isEqualTo(player1.getId());

        harness.passBothPriorities();
        harness.assertLife(player1, 27);
        harness.passBothPriorities();
        harness.assertLife(player1, 34);
    }

    @Test
    @DisplayName("Mode 0 cannot target a creature spell")
    void mode0CannotTargetCreatureSpell() {
        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears, new ChoreographedSparks()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        giveSparksMana(player1);

        harness.castCreature(player1, 0); // creature spell on the stack

        UUID bearsId = bears.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mode 0 cannot target a spell you don't control")
    void mode0CannotTargetSpellYouDontControl() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new ChoreographedSparks()));
        giveSparksMana(player2);

        harness.castInstant(player1, 0); // player1's Angel's Mercy on the stack
        harness.passPriority(player1);

        UUID mercyId = mercy.getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, 0, mercyId))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Mode 1 makes a hasty token whose end-step sacrifice uses the stack")
    void mode1CreatesHastyTokenSacrificedAtEndStep() {
        RuneclawBear bears = new RuneclawBear();
        harness.setHand(player1, List.of(bears, new ChoreographedSparks()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        giveSparksMana(player1);

        harness.castCreature(player1, 0); // Runeclaw Bear creature spell on the stack
        harness.castInstant(player1, 0, 1, bears.getId()); // Choreographed Sparks, mode 1

        // Resolve Choreographed Sparks → creates a creature-spell copy on the stack.
        harness.passBothPriorities();
        StackEntry copy = gd.stack.getLast();
        assertThat(copy.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(copy.getDescription()).isEqualTo("Copy of Runeclaw Bear");

        // Resolve the copy → a token enters the battlefield.
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.hasKeyword(Keyword.HASTE)).isTrue();

        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(null, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        });
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }


    @Test
    @DisplayName("Mode 2 copies one instant/sorcery spell and one creature spell you control")
    void mode2CopiesBoth() {
        RuneclawBear bears = new RuneclawBear();
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(bears, mercy, new ChoreographedSparks()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);
        giveSparksMana(player1);

        harness.castCreature(player1, 0); // Runeclaw Bear creature spell on the stack
        harness.castInstant(player1, 0);  // Angel's Mercy instant on the stack
        harness.castModalInstant(player1, 0, 2, List.of(mercy.getId(), bears.getId())); // mode 2 — both

        // Resolve Choreographed Sparks → both copies are created.
        harness.passBothPriorities();

        assertThat(gd.stack).anySatisfy(se ->
                assertThat(se.getDescription()).isEqualTo("Copy of Angel's Mercy"));
        assertThat(gd.stack).anySatisfy(se ->
                assertThat(se.getDescription()).isEqualTo("Copy of Runeclaw Bear"));

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 27);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().isToken()).isTrue();
                    assertThat(permanent.hasKeyword(Keyword.HASTE)).isTrue();
                });
    }

    @Test
    void creatureModeCannotTargetInstant() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new ChoreographedSparks()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        giveSparksMana(player1);
        harness.castInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, mercy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void instantCopyCanChooseNewTargetWithoutChangingOriginal() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt, new ChoreographedSparks()));
        giveSparksMana(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    void instantCopyCanKeepOriginalTarget() {
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player1, List.of(bolt, new ChoreographedSparks()));
        giveSparksMana(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, 0, bolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    void creatureModeCannotTargetOpponentsCreatureSpell() {
        RuneclawBear bear = new RuneclawBear();
        harness.setHand(player1, List.of(bear));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new ChoreographedSparks()));
        giveSparksMana(player2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    @DisplayName("Choreographed Sparks can't be copied by another copy spell")
    void cannotBeCopied() {
        AngelsMercy mercy = new AngelsMercy();
        harness.setHand(player1, List.of(mercy, new ChoreographedSparks()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        giveSparksMana(player1);

        harness.setHand(player2, List.of(new Twincast()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0); // Angel's Mercy
        ChoreographedSparks sparksInHand = (ChoreographedSparks) gd.playerHands.get(player1.getId()).get(0);
        harness.castInstant(player1, 0, 0, mercy.getId()); // Choreographed Sparks targeting Angel's Mercy
        harness.passPriority(player1);

        // player2 Twincasts the Choreographed Sparks spell.
        harness.castInstant(player2, 0, sparksInHand.getId());

        // Resolve Twincast — it must NOT create a copy of Choreographed Sparks.
        harness.passBothPriorities();

        assertThat(gd.stack).noneMatch(se -> se.getDescription() != null
                && se.getDescription().equals("Copy of Choreographed Sparks"));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("can't be copied"));
    }
}
