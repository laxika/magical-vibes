package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmoredPegasus;
import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudchaserEagle.class, Propaganda.class, ArmoredPegasus.class})
class CloudchaserEagleTest extends BaseCardTest {

    // ===== ETB destroy target enchantment =====

    @Test
    @DisplayName("Resolving enters battlefield and puts ETB destroy on the stack")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new Propaganda());
        harness.setHand(player1, List.of(new CloudchaserEagle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Propaganda");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Cloudchaser Eagle");
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys the target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new Propaganda());
        harness.setHand(player1, List.of(new CloudchaserEagle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Propaganda");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Propaganda");
        harness.assertInGraveyard(player2, "Propaganda");
    }

    @Test
    @DisplayName("Can choose its controller's enchantment after entering without a cast-time target")
    void canChooseOwnEnchantmentAtTriggerTime() {
        harness.addToBattlefield(player1, new Propaganda());
        harness.setHand(player1, List.of(new CloudchaserEagle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Propaganda");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Propaganda");
        harness.assertInGraveyard(player1, "Propaganda");
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new ArmoredPegasus());
        harness.setHand(player1, List.of(new CloudchaserEagle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID creatureId = harness.getPermanentId(player2, "Armored Pegasus");
        assertThatThrownBy(() -> harness.castCreature(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("ETB does not trigger when no enchantment exists")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new CloudchaserEagle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertOnBattlefield(player1, "Cloudchaser Eagle");
        assertThat(gd.stack).isEmpty();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target enchantment is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new Propaganda());
        harness.setHand(player1, List.of(new CloudchaserEagle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player2, "Propaganda");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }
}
