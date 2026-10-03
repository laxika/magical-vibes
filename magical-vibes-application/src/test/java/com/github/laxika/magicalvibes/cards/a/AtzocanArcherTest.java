package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.h.HeadwaterSentries;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtzocanArcher.class, HeadwaterSentries.class, AncientBrontodon.class, RaptorCompanion.class})
class AtzocanArcherTest extends BaseCardTest {

    @Nested
    @DisplayName("ETB fight accepted")
    @CardUsed({AtzocanArcher.class, HeadwaterSentries.class, AncientBrontodon.class, RaptorCompanion.class})
    class FightAccepted {

        @Test
        @DisplayName("ETB triggered ability goes on the stack")
        void etbTriggersOnStack() {
            Permanent target = addCreature(player2);
            castArcherAndResolveSpell();
            harness.handlePermanentChosen(player1, target.getId());

            harness.assertOnBattlefield(player1, "Atzocan Archer");
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(target.getMarkedDamage()).isZero();
            assertThat(archer().getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Fighting marks damage on both surviving creatures")
        void fightMarksDamageOnBothCreatures() {
            Permanent target = addCreature(player2);
            castArcherAndAcceptMay(target.getId());

            harness.assertOnBattlefield(player2, "Headwater Sentries");
            harness.assertOnBattlefield(player1, "Atzocan Archer");
            assertThat(target.getMarkedDamage()).isEqualTo(1);
            assertThat(archer().getMarkedDamage()).isEqualTo(2);
        }

        @Test
        @DisplayName("Archer dies when fighting a bigger creature")
        void archerDiesWhenFightingBiggerCreature() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new AncientBrontodon());
            castArcherAndAcceptMay(target.getId());

            harness.assertInGraveyard(player1, "Atzocan Archer");
            harness.assertNotOnBattlefield(player1, "Atzocan Archer");
            harness.assertOnBattlefield(player2, "Ancient Brontodon");
            assertThat(target.getMarkedDamage()).isEqualTo(1);
        }

        @Test
        @DisplayName("Can fight own creature")
        void canFightOwnCreature() {
            Permanent ownCreature = addCreature(player1);
            castArcherAndAcceptMay(ownCreature.getId());

            harness.assertOnBattlefield(player1, "Headwater Sentries");
            harness.assertOnBattlefield(player1, "Atzocan Archer");
            assertThat(ownCreature.getMarkedDamage()).isEqualTo(1);
            assertThat(archer().getMarkedDamage()).isEqualTo(2);
        }

        @Test
        void killsOneToughnessCreatureAndStillTakesItsDamage() {
            Permanent target = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
            castArcherAndAcceptMay(target.getId());

            harness.assertInGraveyard(player2, "Raptor Companion");
            harness.assertNotOnBattlefield(player2, "Raptor Companion");
            harness.assertOnBattlefield(player1, "Atzocan Archer");
            assertThat(archer().getMarkedDamage()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("ETB fight declined")
    @CardUsed({AtzocanArcher.class, HeadwaterSentries.class})
    class FightDeclined {

        @Test
        @DisplayName("Declining the may ability leaves both creatures unharmed")
        void declineMayLeavesCreatureAlive() {
            Permanent target = addCreature(player2);
            castArcherAndResolveSpell();
            harness.handlePermanentChosen(player1, target.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            harness.assertOnBattlefield(player2, "Headwater Sentries");
            harness.assertOnBattlefield(player1, "Atzocan Archer");
            assertThat(target.getMarkedDamage()).isZero();
            assertThat(archer().getMarkedDamage()).isZero();
        }
    }

    @Test
    void cannotTargetItself() {
        Permanent target = addCreature(player2);
        castArcherAndResolveSpell();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, archer().getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(archer().getMarkedDamage()).isZero();
    }

    @Test
    void noFightWhenThereIsNoOtherCreature() {
        castArcherAndResolveSpell();

        harness.assertOnBattlefield(player1, "Atzocan Archer");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(archer().getMarkedDamage()).isZero();
    }

    @Test
    void noDamageWhenSourceLeavesBeforeResolution() {
        Permanent target = addCreature(player2);
        castArcherAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        Permanent source = archer();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Headwater Sentries");
        harness.assertInGraveyard(player1, "Atzocan Archer");
    }

    @Test
    void noDamageWhenTargetLeavesBeforeResolution() {
        Permanent target = addCreature(player2);
        castArcherAndResolveSpell();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(archer().getMarkedDamage()).isZero();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HeadwaterSentries());
    }

    private Permanent archer() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Atzocan Archer"))
                .findFirst().orElseThrow();
    }

    private void castArcherAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AtzocanArcher()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castArcherAndAcceptMay(UUID targetId) {
        castArcherAndResolveSpell();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }
}
