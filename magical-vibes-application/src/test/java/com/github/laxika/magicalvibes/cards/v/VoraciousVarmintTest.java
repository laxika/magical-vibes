package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoraciousVarmint.class, GloriousAnthem.class, GrizzlyBears.class, LeoninScimitar.class})
class VoraciousVarmintTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Voracious Varmint and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyVarmint(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Voracious Varmint");
        harness.assertInGraveyard(player1, "Voracious Varmint");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyVarmint(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent target = addEnchantment(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        addReadyVarmint(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyVarmint(player1);
        Permanent target = addArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyVarmint(Player player) {
        return addCreatureReady(player, new VoraciousVarmint());
    }

    private Permanent addArtifact(Player player) {
        return addCreatureReady(player, new LeoninScimitar());
    }

    private Permanent addEnchantment(Player player) {
        return addCreatureReady(player, new GloriousAnthem());
    }

    @Test
    void canActivateWhileTappedAndSummoningSickUsingColoredMana() {
        Permanent varmint = harness.addToBattlefieldAndReturn(player1, new VoraciousVarmint());
        varmint.setSummoningSick(true);
        varmint.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voracious Varmint");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    void canDestroyAnArtifactYouControl() {
        addReadyVarmint(player1);
        Permanent target = addArtifact(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Voracious Varmint");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void sacrificesRemainPaidWhenAnotherActivationDestroysTheTargetFirst() {
        addReadyVarmint(player1);
        addReadyVarmint(player1);
        Permanent target = addArtifact(player2);
        Permanent otherArtifact = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.activateAbility(player1, 0, null, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof VoraciousVarmint).hasSize(2);
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact).doesNotContain(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingWithVigilanceDoesNotTapVarmint() {
        Permanent varmint = addReadyVarmint(player1);

        declareAttackers(List.of(0));

        assertThat(varmint.isTapped()).isFalse();
    }
}
