package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvenCloudchaserTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Aven Cloudchaser has correct card properties")
    void hasCorrectProperties() {
        AvenCloudchaser card = new AvenCloudchaser();

        assertThat(card.getName()).isEqualTo("Aven Cloudchaser");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.BIRD, CardSubtype.SOLDIER);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getOnEnterBattlefieldEffects()).hasSize(1);
        assertThat(card.getOnEnterBattlefieldEffects().getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Aven Cloudchaser puts it on the stack with target")
    void castingPutsItOnStackWithTarget() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Aven Cloudchaser");
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Aven Cloudchaser enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aven Cloudchaser"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Aven Cloudchaser");
        assertThat(trigger.getTargetPermanentId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("Can destroy own enchantment with ETB")
    void canDestroyOwnEnchantment() {
        harness.addToBattlefield(player1, new AngelicChorus());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        UUID targetId = harness.getPermanentId(player1, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        harness.passBothPriorities(); // resolve creature → ETB + Angelic Chorus trigger pushed
        harness.passBothPriorities(); // resolve Angelic Chorus trigger (top of stack)
        harness.passBothPriorities(); // resolve ETB (destroy enchantment)

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Angelic Chorus"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Angelic Chorus"));
    }

    @Test
    @DisplayName("ETB fizzles if target enchantment is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no enchantments on battlefield")
    void canCastWithoutTargetWhenNoEnchantments() {
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aven Cloudchaser");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Creature should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Aven Cloudchaser"));
        // No triggered ability on stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Aven Cloudchaser has flying keyword on the battlefield")
    void hasFlying() {
        harness.addToBattlefield(player1, new AvenCloudchaser());

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()).getFirst()
                .hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new AvenCloudchaser()));
        harness.addMana(player1, "W", 2);

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }
}
