package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrashingBrontodon.class, ShortSword.class, GloriousAnthem.class, AlpineWatchdog.class})
class ThrashingBrontodonTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices Thrashing Brontodon and destroys target artifact")
    void destroysTargetArtifact() {
        addReadyBrontodon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent target = addArtifact(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Thrashing Brontodon");
        harness.assertInGraveyard(player1, "Thrashing Brontodon");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Short Sword");
        harness.assertInGraveyard(player2, "Short Sword");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysTargetEnchantment() {
        addReadyBrontodon(player1);
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
        addReadyBrontodon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent creature = addCreatureReady(player2, new AlpineWatchdog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyBrontodon(player1);
        Permanent target = addArtifact(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while tapped and summoning sick using colored mana")
    void activatesWhileTappedAndSummoningSick() {
        Permanent brontodon = harness.addToBattlefieldAndReturn(player1, new ThrashingBrontodon());
        brontodon.setSummoningSick(true);
        brontodon.tap();
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Thrashing Brontodon");
        harness.assertOnBattlefield(player2, "Short Sword");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Short Sword");
    }

    @Test
    @DisplayName("Can destroy an artifact controlled by its controller")
    void destroysOwnArtifact() {
        addReadyBrontodon(player1);
        Permanent target = addArtifact(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thrashing Brontodon");
        harness.assertInGraveyard(player1, "Short Sword");
    }

    @Test
    @DisplayName("Sacrifice remains paid when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        addReadyBrontodon(player1);
        Permanent target = addArtifact(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thrashing Brontodon");
        harness.assertNotInGraveyard(player2, "Short Sword");
        assertThat(gd.playerHands.get(player2.getId())).contains(target.getCard());
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBrontodon(Player player) {
        return addCreatureReady(player, new ThrashingBrontodon());
    }

    private Permanent addArtifact(Player player) {
        return addCreatureReady(player, new ShortSword());
    }

    private Permanent addEnchantment(Player player) {
        return addCreatureReady(player, new GloriousAnthem());
    }
}
