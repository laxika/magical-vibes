package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonRoostTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Dragon Roost has correct card properties")
    void hasCorrectProperties() {
        DragonRoost card = new DragonRoost();

        assertThat(card.getName()).isEqualTo("Dragon Roost");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{4}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{5}{R}{R}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect tokenEffect = (CreateCreatureTokenEffect) ability.getEffects().getFirst();
        assertThat(tokenEffect.tokenName()).isEqualTo("Dragon");
        assertThat(tokenEffect.power()).isEqualTo(5);
        assertThat(tokenEffect.toughness()).isEqualTo(5);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.RED);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.FLYING);
        assertThat(tokenEffect.additionalTypes()).isEmpty();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Dragon Roost puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new DragonRoost()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Dragon Roost");
    }

    @Test
    @DisplayName("Resolving puts Dragon Roost onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new DragonRoost()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dragon Roost"));
    }

    // ===== Token creation via activated ability =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Dragon Roost");
    }

    @Test
    @DisplayName("Resolving ability creates a 5/5 Dragon token")
    void resolvingAbilityCreatesToken() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent token = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(5);
        assertThat(token.getCard().getToughness()).isEqualTo(5);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
    }

    @Test
    @DisplayName("Dragon token has flying")
    void dragonTokenHasFlying() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Dragon token is a creature")
    void dragonTokenIsCreature() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .findFirst().orElseThrow();
        assertThat(gs.isCreature(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Dragon token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .findFirst().orElseThrow();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 9);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Can activate ability multiple times with enough mana")
    void canActivateMultipleTimes() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 14);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Dragon"))
                .count();
        assertThat(tokenCount).isEqualTo(2);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Dragon Roost stays on battlefield =====

    @Test
    @DisplayName("Dragon Roost remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Dragon Roost"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Creating Dragon token is logged")
    void tokenCreationIsLogged() {
        addDragonRoostReady(player1);
        harness.addMana(player1, ManaColor.RED, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Dragon") && log.contains("token"));
    }

    // ===== Helper methods =====

    private Permanent addDragonRoostReady(Player player) {
        DragonRoost card = new DragonRoost();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
