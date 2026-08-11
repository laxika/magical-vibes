package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class AegisOfHonorTest extends BaseCardTest {

    @Test
    @DisplayName("The next instant or sorcery spell damage to you is redirected to its controller")
    void redirectsInstantOrSorceryDamageToItsController() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateAegis(aegis);
        castShockAt(player2, player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The shield is consumed after one spell damage event")
    void redirectsOnlyTheNextSpellDamageEvent() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateAegis(aegis);
        castShockAt(player2, player1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage from an activated ability is not redirected")
    void doesNotRedirectActivatedAbilityDamage() {
        Permanent aegis = addReadyAegis(player1);
        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancer);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateAegis(aegis);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyAegis(Player player) {
        Permanent aegis = new Permanent(new AegisOfHonor());
        aegis.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(aegis);
        return aegis;
    }

    private void activateAegis(Permanent aegis) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aegis), null, null);
        harness.passBothPriorities();
    }

    private void castShockAt(Player caster, Player target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
