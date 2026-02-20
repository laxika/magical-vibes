package com.github.laxika.magicalvibes.cards.t;

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
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TheHiveTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("The Hive has correct card properties")
    void hasCorrectProperties() {
        TheHive card = new TheHive();

        assertThat(card.getName()).isEqualTo("The Hive");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{5}");
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{5}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect tokenEffect = (CreateCreatureTokenEffect) ability.getEffects().getFirst();
        assertThat(tokenEffect.tokenName()).isEqualTo("Wasp");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isNull();
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.INSECT);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.FLYING);
        assertThat(tokenEffect.additionalTypes()).containsExactly(CardType.ARTIFACT);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingPutsOnStack() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("The Hive");
    }

    @Test
    @DisplayName("Activating ability taps The Hive")
    void activatingTapsHive() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        Permanent hive = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(hive.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(hive.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution: token properties =====

    @Test
    @DisplayName("Resolving ability creates a 1/1 Wasp token")
    void resolvingCreatesWaspToken() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent token = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Wasp"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
    }

    @Test
    @DisplayName("Wasp token has flying")
    void waspTokenHasFlying() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wasp"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Wasp token is also an artifact")
    void waspTokenIsAlsoArtifact() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wasp"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Wasp token is a creature")
    void waspTokenIsCreature() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wasp"))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, token)).isTrue();
    }

    @Test
    @DisplayName("Wasp token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wasp"))
                .findFirst().orElseThrow();
        assertThat(token.isSummoningSick()).isTrue();
    }

    // ===== Multiple tokens =====

    @Test
    @DisplayName("Can create multiple Wasp tokens across turns")
    void canCreateMultipleTokens() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Wasp"))
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate twice because it requires tap")
    void cannotActivateTwice() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 10);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== The Hive stays on battlefield =====

    @Test
    @DisplayName("The Hive remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("The Hive"));
    }

    // ===== Game log =====

    @Test
    @DisplayName("Creating Wasp token is logged")
    void tokenCreationIsLogged() {
        addHiveReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Wasp") && log.contains("token"));
    }

    // ===== Helper methods =====

    private Permanent addHiveReady(Player player) {
        TheHive card = new TheHive();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

