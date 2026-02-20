package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SiegeGangCommanderTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Siege-Gang Commander has correct card properties and ability structure")
    void hasCorrectProperties() {
        SiegeGangCommander card = new SiegeGangCommander();

        assertThat(card.getName()).isEqualTo("Siege-Gang Commander");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.GOBLIN);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateCreatureTokenEffect.class);
        CreateCreatureTokenEffect tokenEffect =
                (CreateCreatureTokenEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(tokenEffect.amount()).isEqualTo(3);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isEqualTo("{1}{R}");
        assertThat(ability.isNeedsTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSubtypeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DealDamageToAnyTargetEffect.class);
    }

    @Test
    @DisplayName("ETB creates three 1/1 red Goblin tokens")
    void etbCreatesThreeGoblinTokens() {
        harness.setHand(player1, List.of(new SiegeGangCommander()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(4);
        assertThat(countGoblinTokens(player1)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating ability with multiple Goblins asks to choose sacrifice")
    void activatingAbilityWithMultipleGoblinsAsksForChoice() {
        castAndResolveCommanderWithTokens();
        UUID targetPlayerId = player2.getId();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, targetPlayerId);

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a token to sacrifice puts ability on stack targeting chosen target")
    void choosingTokenToSacrificePutsAbilityOnStack() {
        castAndResolveCommanderWithTokens();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        UUID tokenId = getAnyGoblinTokenId(player1);
        harness.handlePermanentChosen(player1, tokenId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Siege-Gang Commander");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(player2.getId());
        assertThat(countGoblinTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target player on resolution")
    void abilityDealsDamageToPlayer() {
        castAndResolveCommanderWithTokens();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, getAnyGoblinTokenId(player1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Ability deals 2 damage to target creature")
    void abilityDealsDamageToCreature() {
        castAndResolveCommanderWithTokens();
        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, elvesId);
        harness.handlePermanentChosen(player1, getAnyGoblinTokenId(player1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Can sacrifice Siege-Gang Commander itself and still deal damage")
    void canSacrificeCommanderItself() {
        castAndResolveCommanderWithTokens();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        UUID commanderId = harness.getPermanentId(player1, "Siege-Gang Commander");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, commanderId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Siege-Gang Commander"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        castAndResolveCommanderWithTokens();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private void castAndResolveCommanderWithTokens() {
        harness.setHand(player1, List.of(new SiegeGangCommander()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private int countGoblinTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.GOBLIN))
                .count();
    }

    private UUID getAnyGoblinTokenId(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Goblin token found"))
                .getId();
    }
}

