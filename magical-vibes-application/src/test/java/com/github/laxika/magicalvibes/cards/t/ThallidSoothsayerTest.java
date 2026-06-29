package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThallidSoothsayerTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Thallid Soothsayer has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        ThallidSoothsayer card = new ThallidSoothsayer();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{2}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DrawCardEffect.class);

        DrawCardEffect draw = (DrawCardEffect) ability.getEffects().get(1);
        assertThat(draw.amount()).isEqualTo(1);
    }

    // ===== Activation: sacrificing a creature and drawing =====

    @Test
    @DisplayName("Activating ability sacrifices chosen creature and puts draw on stack")
    void activatingAbilitySacrificesCreatureAndPutsDrawOnStack() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        // Two creatures available — player must choose which to sacrifice
        harness.handlePermanentChosen(player1, bearsId);

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Thallid Soothsayer should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thallid Soothsayer"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Thallid Soothsayer");
    }

    @Test
    @DisplayName("Activating ability does NOT tap the Soothsayer")
    void activatingDoesNotTap() {
        Permanent soothsayer = addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 2);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(soothsayer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability multiple times in a turn with enough creatures and mana")
    void canActivateMultipleTimes() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest(), new Forest()));

        // First activation — 3 creatures, must choose
        UUID bears1Id = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears1Id);
        harness.passBothPriorities();

        // Second activation — 2 creatures remain (Soothsayer + Bears), must choose
        UUID bears2Id = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears2Id);
        harness.passBothPriorities();

        // Both Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Should have drawn 2 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability draws a card")
    void resolvingDrawsACard() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest()));

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Soothsayer remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLACK, 2);
        setDeck(player1, List.of(new Forest()));

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thallid Soothsayer"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySoothsayer(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can sacrifice the Soothsayer itself to its own ability")
    void canSacrificeItself() {
        addReadySoothsayer(player1);
        // Soothsayer is the only creature — auto-picks itself
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Thallid Soothsayer"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Thallid Soothsayer"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadySoothsayer(Player player) {
        ThallidSoothsayer card = new ThallidSoothsayer();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<? extends Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
