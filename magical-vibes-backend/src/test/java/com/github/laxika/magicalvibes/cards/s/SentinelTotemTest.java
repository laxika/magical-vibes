package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileAllGraveyardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SentinelTotemTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Sentinel Totem has scry 1 ETB effect")
    void hasScryEtbEffect() {
        SentinelTotem card = new SentinelTotem();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ScryEffect.class);
        ScryEffect effect = (ScryEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sentinel Totem has tap + exile self activated ability with ExileAllGraveyardsEffect")
    void hasExileGraveyardsAbility() {
        SentinelTotem card = new SentinelTotem();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects())
                .hasSize(2)
                .anyMatch(e -> e instanceof ExileSelfCost)
                .anyMatch(e -> e instanceof ExileAllGraveyardsEffect);
    }

    // ===== ETB scry =====

    @Test
    @DisplayName("Casting and resolving Sentinel Totem triggers scry 1")
    void castingTriggersScry() {
        harness.setHand(player1, List.of(new SentinelTotem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sentinel Totem"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ETB enters scry state with 1 card")
    void resolvingEtbEntersScryState() {
        harness.setHand(player1, List.of(new SentinelTotem()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve artifact
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);
    }

    // ===== Activated ability: exile all graveyards =====

    @Test
    @DisplayName("Activating ability exiles Sentinel Totem as cost and puts ability on stack")
    void activatingExilesSelfAndPutsOnStack() {
        Permanent totem = addReadyTotem(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        // Totem is exiled as cost
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sentinel Totem"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sentinel Totem"));
        // Ability is on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability exiles all cards from all graveyards")
    void resolvingExilesAllGraveyards() {
        addReadyTotem(player1);

        // Put cards in both players' graveyards
        GrizzlyBears bears = new GrizzlyBears();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(bolt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Both graveyards should be empty
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        // Cards should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Resolving ability with empty graveyards does not error")
    void resolvingWithEmptyGraveyards() {
        addReadyTotem(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sentinel Totem goes to exile, not graveyard, as cost")
    void totemGoesToExileNotGraveyard() {
        addReadyTotem(player1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Sentinel Totem"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Sentinel Totem"));
    }

    // ===== Tap requirement =====

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent totem = addReadyTotem(player1);
        totem.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles only the opponent's graveyard when controller's graveyard is empty")
    void exilesOnlyOpponentGraveyard() {
        addReadyTotem(player1);

        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bolt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Exiles multiple cards from a single graveyard")
    void exilesMultipleCardsFromSingleGraveyard() {
        addReadyTotem(player1);

        GrizzlyBears bears = new GrizzlyBears();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bears, bolt));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    // ===== Helpers =====

    private Permanent addReadyTotem(Player player) {
        SentinelTotem card = new SentinelTotem();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
