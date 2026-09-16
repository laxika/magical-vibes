package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Chainflinger;
import com.github.laxika.magicalvibes.cards.e.EmberBeast;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.v.VolcanicSpray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AegisOfHonor.class, Chainflinger.class, EmberBeast.class, Firebolt.class,
        FlameBurst.class, VolcanicSpray.class})
class AegisOfHonorTest extends BaseCardTest {

    @Test
    @DisplayName("The next instant or sorcery spell damage to you is redirected to its controller")
    void redirectsInstantOrSorceryDamageToItsController() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateAegis(aegis);
        castFlameBurstAt(player2, player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The shield is consumed after one spell damage event")
    void redirectsOnlyTheNextSpellDamageEvent() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        activateAegis(aegis);
        castFireboltAt(player2, player1);
        castFireboltAt(player2, player1);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage from an activated ability is not redirected")
    void doesNotRedirectActivatedAbilityDamage() {
        Permanent aegis = addReadyAegis(player1);
        Permanent chainflinger = addCreatureReady(player2, new Chainflinger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateAegis(aegis);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(chainflinger),
                0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Sorcery damage to a permanent does not consume the shield")
    void doesNotConsumeShieldForDamageToPermanent() {
        Permanent aegis = addReadyAegis(player1);
        Permanent target = addCreatureReady(player1, new EmberBeast());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        activateAegis(aegis);
        castFireboltAt(player2, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);

        castFireboltAt(player2, player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The shield expires when the turn ends")
    void shieldExpiresAtEndOfTurn() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        activateAegis(aegis);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        castFireboltAt(player2, player1);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Only the protected player's damage is redirected from a spell that damages each player")
    void redirectsOnlyProtectedPlayerDamageFromMassSpell() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        activateAegis(aegis);
        harness.setHand(player2, List.of(new VolcanicSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Each activation creates a separate one-shot shield")
    void multipleActivationsCreateMultipleShields() {
        Permanent aegis = addReadyAegis(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        activateAegis(aegis);
        activateAegis(aegis);
        castFireboltAt(player2, player1);
        castFireboltAt(player2, player1);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 16);
    }

    private Permanent addReadyAegis(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AegisOfHonor());
    }

    private void activateAegis(Permanent aegis) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aegis), null, null);
        harness.passBothPriorities();
    }

    private void castFlameBurstAt(Player caster, Player target) {
        harness.setHand(caster, List.of(new FlameBurst()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    private void castFireboltAt(Player caster, Player target) {
        castFireboltAt(caster, target.getId());
    }

    private void castFireboltAt(Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Firebolt()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveSorcery(caster, 0, targetId);
    }
}
