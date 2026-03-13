package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VisceraSeerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has sacrifice-a-creature activated ability with scry 1")
    void hasCorrectAbilityStructure() {
        VisceraSeer card = new VisceraSeer();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(ScryEffect.class);
        ScryEffect effect = (ScryEffect) ability.getEffects().get(1);
        assertThat(effect.count()).isEqualTo(1);
    }

    // ===== Activation: sacrifice a creature to scry 1 =====

    @Test
    @DisplayName("Sacrificing a creature enters scry state with 1 card")
    void sacrificeCreatureTriggersScry() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        // Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Should be in scry state
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(1);
    }

    @Test
    @DisplayName("Scry 1 keeping card on top preserves it")
    void scryKeepOnTop() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        assertThat(deck.get(0)).isSameAs(topCard);
    }

    @Test
    @DisplayName("Scry 1 putting card on bottom moves it")
    void scryPutOnBottom() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0));

        assertThat(deck.get(0)).isNotSameAs(topCard);
        assertThat(deck.get(deck.size() - 1)).isSameAs(topCard);
    }

    @Test
    @DisplayName("Can sacrifice Viscera Seer to its own ability")
    void canSacrificeItself() {
        addReadySeer(player1);
        UUID seerId = harness.getPermanentId(player1, "Viscera Seer");

        harness.activateAbility(player1, 0, null, seerId);

        // Seer should be sacrificed, ability on stack
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Viscera Seer"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability works while tapped (no tap cost)")
    void worksWhileTapped() {
        Permanent seer = addReadySeer(player1);
        seer.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutSacrificeTarget() {
        addReadySeer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate multiple times per turn with different creatures")
    void canActivateMultipleTimes() {
        addReadySeer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        GrizzlyBears bears2 = new GrizzlyBears();
        Permanent bears2Perm = new Permanent(bears2);
        bears2Perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears2Perm);

        UUID bears1Id = harness.getPermanentId(player1, "Grizzly Bears");

        // First activation
        harness.activateAbility(player1, 0, null, bears1Id);
        harness.passBothPriorities();

        // Complete scry
        GameData gd = harness.getGameData();
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0), List.of());

        // Second activation with the other bears
        UUID bears2Id = bears2Perm.getId();
        harness.activateAbility(player1, 0, null, bears2Id);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);

        // Both bears should be in graveyard
        long bearsInGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears"))
                .count();
        assertThat(bearsInGraveyard).isEqualTo(2);
    }

    // ===== Helpers =====

    private Permanent addReadySeer(Player player) {
        VisceraSeer card = new VisceraSeer();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
