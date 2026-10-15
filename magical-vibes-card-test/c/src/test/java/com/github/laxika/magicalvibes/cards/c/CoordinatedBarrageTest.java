package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoordinatedBarrage.class, AirElemental.class, ChangelingSentinel.class, CloakAndDagger.class, FurnaceOfRath.class,
        GrizzlyBears.class, HillGiant.class})
class CoordinatedBarrageTest extends BaseCardTest {

    /** Puts an attacking creature on player1's battlefield and hands player2 the spell + {W}. */
    private Permanent setupAttackerAndSpell(Card attackerCard) {
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CoordinatedBarrage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);
        return attacker;
    }

    @Test
    @DisplayName("Deals damage equal to the number of permanents of the chosen type you control")
    void damageEqualsChosenTypeCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "BEAR");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Furnace of Rath doubles the chosen-type count damage")
    void furnaceOfRathDoublesDamage() {
        harness.addToBattlefield(player2, new FurnaceOfRath());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "BEAR");

        // One Bear -> 1 damage, doubled to 2 by Furnace of Rath.
        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 2);
    }

    @Test
    @DisplayName("Lethal damage from the chosen-type count destroys the target")
    void lethalCountKillsTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new HillGiant());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "BEAR");

        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Choosing a type you control none of deals no damage")
    void chosenTypeYouControlNoneDealsZero() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 0);
    }

    @Test
    @DisplayName("Does not count matching permanents controlled by an opponent")
    void doesNotCountOpponentsPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "BEAR");

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A Changeling you control counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player2, new ChangelingSentinel());

        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "GOBLIN");

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental") && p.getMarkedDamage() == 1);
    }

    @Test
    @DisplayName("Cannot target a player — the damage is declared at a creature, not at any target")
    void cannotTargetAPlayer() {
        setupAttackerAndSpell(new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CoordinatedBarrage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Can target a blocking creature")
    void canTargetBlockingCreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new AirElemental());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.setHand(player2, List.of(new CoordinatedBarrage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, blocker.getId());
        harness.handleListChoice(player2, "ELEMENTAL");

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts noncreature kindred permanents of the chosen type")
    void countsKindredEquipment() {
        harness.addToBattlefield(player2, new CloakAndDagger());
        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.handleListChoice(player2, "ROGUE");

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts matching permanents when the spell resolves")
    void countsPermanentsAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castInstant(player2, 0, target.getId());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "BEAR");

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not resolve if its target is no longer attacking or blocking")
    void targetLeavingCombatPreventsResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = setupAttackerAndSpell(new AirElemental());

        harness.castInstant(player2, 0, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Coordinated Barrage");
    }
}
