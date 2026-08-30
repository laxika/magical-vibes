package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EbonyRhino;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SeasClaim;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Marjhan.class, Island.class, EbonyRhino.class, WillowFaerie.class, Forest.class, SeasClaim.class})
class MarjhanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.setHand(player1, List.of(new Marjhan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature -> state trigger fires
        harness.passBothPriorities(); // resolve state trigger -> sacrificed

        harness.assertNotOnBattlefield(player1, "Marjhan");
        harness.assertInGraveyard(player1, "Marjhan");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new Marjhan()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Marjhan");
    }

    @Test
    @DisplayName("Tapped Marjhan does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);

        advanceToUpkeep(player1);

        assertThat(marjhan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing a creature during upkeep untaps Marjhan")
    void sacrificingUntapsMarjhanDuringUpkeep() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);
        Permanent rhino = harness.addToBattlefieldAndReturn(player1, new EbonyRhino());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.activateAbility(player1, marjhanIndex, 0, null, null);
        harness.handlePermanentChosen(player1, rhino.getId());
        harness.passBothPriorities();

        assertThat(marjhan.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Ebony Rhino");
    }

    @Test
    @DisplayName("The untap ability cannot be activated outside the controller's upkeep")
    void untapAbilityRestrictedToUpkeep() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);
        harness.addToBattlefield(player1, new EbonyRhino());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 2);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        assertThatThrownBy(() -> harness.activateAbility(player1, marjhanIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(marjhan.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The untap ability may sacrifice Marjhan itself")
    void untapAbilityMaySacrificeMarjhanItself() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLUE, 2);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.activateAbility(player1, marjhanIndex, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marjhan");
        harness.assertInGraveyard(player1, "Marjhan");
    }

    @Test
    @DisplayName("{U}{U} shrinks Marjhan and deals 1 damage to an attacking creature without flying")
    void damagesAttackingNonFlyer() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, false);
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new EbonyRhino());
        rhino.setSummoningSick(false);

        declareAttack(rhino);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, marjhanIndex, 1, null, rhino.getId());
        harness.passBothPriorities();

        assertThat(marjhan.getPowerModifier()).isEqualTo(-1);
        assertThat(rhino.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The damage ability cannot target an attacking creature with flying")
    void cannotTargetFlyer() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, false);
        Permanent faerie = harness.addToBattlefieldAndReturn(player2, new WillowFaerie());
        faerie.setSummoningSick(false);

        declareAttack(faerie);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, marjhanIndex, 1, null, faerie.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The damage ability cannot target a nonattacking creature")
    void cannotTargetNonattackingCreature() {
        harness.addToBattlefield(player1, new Island());
        Permanent marjhan = addMarjhan(player1, false);
        Permanent rhino = harness.addToBattlefieldAndReturn(player2, new EbonyRhino());

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, marjhanIndex, 1, null, rhino.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("State-triggered sacrifice still resolves after an Island enters")
    void stateTriggeredSacrificeIsNotUndoneByIslandEntering() {
        harness.setHand(player1, List.of(new Marjhan()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Marjhan");
        harness.assertInGraveyard(player1, "Marjhan");
    }

    @Test
    @DisplayName("A land that becomes an Island satisfies Marjhan's Island condition")
    void landThatBecomesIslandSatisfiesCondition() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new SeasClaim(), new Marjhan()));
        harness.addMana(player1, ManaColor.BLUE, 8);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Marjhan");
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent marjhan = addMarjhan(player1, false);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        declareAttackers(List.of(marjhanIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent marjhan = addMarjhan(player1, false);

        int marjhanIndex = gd.playerBattlefields.get(player1.getId()).indexOf(marjhan);
        assertThatThrownBy(() -> declareAttackers(List.of(marjhanIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareAttack(Permanent attacker) {
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        declareAttackers(player2, List.of(index));
    }

    private Permanent addMarjhan(Player player, boolean tapped) {
        Permanent perm = new Permanent(new Marjhan());
        perm.setSummoningSick(false);
        if (tapped) {
            perm.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

}
