package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WakeOfVultures.class})
class WakeOfVulturesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature regenerates Wake of Vultures")
    void sacrificingCreatureRegenerates() {
        Permanent vultures = addCreatureReady(player1, new WakeOfVultures());
        Permanent fodder = addCreatureReady(player1, new WakeOfVultures());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(vultures.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(fodder.getId()));
        harness.assertOnBattlefield(player1, "Wake of Vultures");
    }

    @Test
    @DisplayName("Wake of Vultures may sacrifice itself as the creature cost")
    void maySacrificeItselfAsCost() {
        Permanent vultures = addCreatureReady(player1, new WakeOfVultures());
        addCreatureReady(player1, new WakeOfVultures());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, vultures.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wake of Vultures");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Regeneration shield saves Wake of Vultures from lethal combat damage")
    void regeneratesFromLethalDamage() {
        addCreatureReady(player1, new WakeOfVultures());
        Permanent fodder = addCreatureReady(player1, new WakeOfVultures());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        Permanent vultures = findPermanent(player1, "Wake of Vultures");
        assertThat(vultures.getRegenerationShield()).isEqualTo(1);

        vultures.setBlocking(true);
        vultures.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new WakeOfVultures());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Wake of Vultures");
        Permanent regenerated = findPermanent(player1, "Wake of Vultures");
        assertThat(regenerated.isTapped()).isTrue();
        assertThat(regenerated.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Without a shield Wake of Vultures dies to lethal combat damage")
    void diesWithoutShield() {
        Permanent vultures = addCreatureReady(player1, new WakeOfVultures());

        vultures.setBlocking(true);
        vultures.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new WakeOfVultures());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertInGraveyard(player1, "Wake of Vultures");
    }
}
