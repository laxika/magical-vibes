package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AtzocanArcherTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Card has MayEffect wrapping SourceFightsTargetCreatureEffect on ETB")
    void hasCorrectEffects() {
        AtzocanArcher card = new AtzocanArcher();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(SourceFightsTargetCreatureEffect.class);
    }

    // ===== ETB fight accepted =====

    @Nested
    @DisplayName("ETB fight accepted")
    class FightAccepted {

        @Test
        @DisplayName("ETB triggered ability goes on the stack")
        void etbTriggersOnStack() {
            Permanent target = addCreature(player2);
            castArcherAndResolveSpell();

            harness.assertOnBattlefield(player1, "Atzocan Archer");
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        }

        @Test
        @DisplayName("Fights and kills a smaller creature")
        void fightsAndKillsSmallerCreature() {
            // Grizzly Bears is 2/2, Atzocan Archer is 1/4
            Permanent target = addCreature(player2);
            castArcherAndAcceptMay(target.getId());

            // Grizzly Bears takes 1 damage (1 power), survives with 2 toughness
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            // Archer takes 2 damage (2 power), survives with 4 toughness
            harness.assertOnBattlefield(player1, "Atzocan Archer");
        }

        @Test
        @DisplayName("Both creatures can die when fighting a bigger creature")
        void archerDiesWhenFightingBiggerCreature() {
            // Add a 4/4 creature
            Permanent target = addSpecificCreature(player2, new com.github.laxika.magicalvibes.cards.h.HillGiant());
            castArcherAndAcceptMay(target.getId());

            // Archer is 1/4, takes 3 damage from Hill Giant (3/3) — survives
            // Hill Giant is 3/3, takes 1 damage from Archer — survives
            harness.assertOnBattlefield(player1, "Atzocan Archer");
            harness.assertOnBattlefield(player2, "Hill Giant");
        }

        @Test
        @DisplayName("Can fight own creature")
        void canFightOwnCreature() {
            Permanent ownCreature = addCreature(player1);
            castArcherAndAcceptMay(ownCreature.getId());

            // Own Grizzly Bears takes 1 damage (Archer has 1 power) — survives with 2 toughness
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            // Archer takes 2 damage (Bears have 2 power) — survives with 4 toughness
            harness.assertOnBattlefield(player1, "Atzocan Archer");
        }
    }

    // ===== ETB fight declined =====

    @Nested
    @DisplayName("ETB fight declined")
    class FightDeclined {

        @Test
        @DisplayName("Declining the may ability leaves both creatures unharmed")
        void declineMayLeavesCreatureAlive() {
            harness.addToBattlefield(player2, new GrizzlyBears());

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player1, List.of(new AtzocanArcher()));
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);

            harness.castCreature(player1, 0);
            harness.passBothPriorities(); // resolve creature spell → ETB trigger
            harness.passBothPriorities(); // resolve MayEffect → may prompt
            harness.handleMayAbilityChosen(player1, false); // decline

            // Grizzly Bears still on battlefield
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            // Archer still entered the battlefield
            harness.assertOnBattlefield(player1, "Atzocan Archer");
        }
    }

    // ===== Helpers =====

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        return addSpecificCreature(player, new GrizzlyBears());
    }

    private Permanent addSpecificCreature(com.github.laxika.magicalvibes.model.Player player,
                                          com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void castArcherAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AtzocanArcher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
    }

    private void castArcherAndAcceptMay(UUID targetId) {
        castArcherAndResolveSpell();
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → target selection
        harness.handlePermanentChosen(player1, targetId); // choose target → fight
    }
}
