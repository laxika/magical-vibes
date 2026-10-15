package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ResoundingThunder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkeletalKathari.class, GrizzlyBears.class, DregscapeZombie.class, ResoundingThunder.class})
class SkeletalKathariTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature and paying {B} sets a regeneration shield on Skeletal Kathari")
    void sacrificingCreatureRegeneratesKathari() {
        Permanent kathari = addKathariReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        // Grizzly Bears is sacrificed
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        // Skeletal Kathari has a regeneration shield
        assertThat(kathari.getRegenerationShield()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability without black mana")
    void cannotActivateWithoutMana() {
        addKathariReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Kathari can regenerate and pays sacrifice before resolution")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent kathari = harness.addToBattlefieldAndReturn(player1, new SkeletalKathari());
        kathari.setSummoningSick(true);
        kathari.tap();
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombie.getId());

        harness.assertNotOnBattlefield(player1, "Dregscape Zombie");
        harness.assertInGraveyard(player1, "Dregscape Zombie");
        assertThat(kathari.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(kathari.getRegenerationShield()).isEqualTo(1);
        assertThat(kathari.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Regeneration replaces lethal damage once, taps Kathari and removes damage")
    void regenerationShieldProtectsAgainstLethalDamageOnce() {
        Permanent kathari = addKathariReady(player1);
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();
        assertThat(kathari.isTapped()).isFalse();

        harness.setHand(player2, List.of(new ResoundingThunder(), new ResoundingThunder()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player2, 0, kathari.getId());

        harness.assertOnBattlefield(player1, "Skeletal Kathari");
        assertThat(kathari.isTapped()).isTrue();
        assertThat(kathari.getMarkedDamage()).isZero();
        assertThat(kathari.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player2, 0, kathari.getId());

        harness.assertNotOnBattlefield(player1, "Skeletal Kathari");
        harness.assertInGraveyard(player1, "Skeletal Kathari");
    }

    @Test
    @DisplayName("Kathari can sacrifice itself and regeneration cannot save it from sacrifice")
    void canSacrificeItselfEvenWithRegenerationShield() {
        Permanent kathari = addKathariReady(player1);
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new DregscapeZombie());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();
        assertThat(kathari.getRegenerationShield()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, kathari.getId());

        harness.assertNotOnBattlefield(player1, "Skeletal Kathari");
        harness.assertInGraveyard(player1, "Skeletal Kathari");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Skeletal Kathari");
        harness.assertInGraveyard(player1, "Skeletal Kathari");
    }

    private Permanent addKathariReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new SkeletalKathari());
        perm.setSummoningSick(false);
        return perm;
    }
}
