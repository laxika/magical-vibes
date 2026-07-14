package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrackleburrTest extends BaseCardTest {

    private static final int DAMAGE_ABILITY = 0;
    private static final int BOUNCE_ABILITY = 1;

    // ===== Damage ability: {T}, tap two red creatures: 3 damage to any target =====

    @Test
    @DisplayName("Deals 3 damage to target player, tapping the source and two red creatures")
    void damageAbilityHitsPlayer() {
        Permanent crackleburr = addReadyCrackleburr(player1);
        Permanent red1 = addRedCreature(player1);
        Permanent red2 = addRedCreature(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        int idx = indexOf(player1, crackleburr);
        harness.activateAbility(player1, idx, DAMAGE_ABILITY, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(crackleburr.isTapped()).isTrue();
        assertThat(red1.isTapped()).isTrue();
        assertThat(red2.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals 3 damage to a target creature, killing a 2/2")
    void damageAbilityKillsCreature() {
        Permanent crackleburr = addReadyCrackleburr(player1);
        addRedCreature(player1);
        addRedCreature(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        int idx = indexOf(player1, crackleburr);
        harness.activateAbility(player1, idx, DAMAGE_ABILITY, null, victim);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate the damage ability without two untapped red creatures")
    void damageAbilityRequiresTwoRedCreatures() {
        Permanent crackleburr = addReadyCrackleburr(player1);
        addRedCreature(player1); // only one red creature available
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        int idx = indexOf(player1, crackleburr);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, DAMAGE_ABILITY, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Bounce ability: {Q}, untap two blue creatures: return target creature =====

    @Test
    @DisplayName("Returns target creature to its owner's hand, untapping the source and two blue creatures")
    void bounceAbilityReturnsCreature() {
        Permanent crackleburr = addReadyCrackleburr(player1);
        crackleburr.tap(); // {Q} requires the source to be tapped
        Permanent blue1 = addTappedBlueCreature(player1);
        Permanent blue2 = addTappedBlueCreature(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        int idx = indexOf(player1, crackleburr);
        harness.activateAbility(player1, idx, BOUNCE_ABILITY, null, victim);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(crackleburr.isTapped()).isFalse();
        assertThat(blue1.isTapped()).isFalse();
        assertThat(blue2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the bounce ability without two tapped blue creatures")
    void bounceAbilityRequiresTwoTappedBlueCreatures() {
        Permanent crackleburr = addReadyCrackleburr(player1);
        crackleburr.tap();
        addTappedBlueCreature(player1); // only one tapped blue creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        int idx = indexOf(player1, crackleburr);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, BOUNCE_ABILITY, null, victim))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadyCrackleburr(Player player) {
        Permanent perm = new Permanent(new Crackleburr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addRedCreature(Player player) {
        Permanent perm = new Permanent(new HillGiant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addTappedBlueCreature(Player player) {
        Permanent perm = new Permanent(new AirElemental());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
