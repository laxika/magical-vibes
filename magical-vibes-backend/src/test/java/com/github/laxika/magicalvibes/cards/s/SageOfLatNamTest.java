package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SageOfLatNamTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has tap + sacrifice artifact cost with draw card activated ability")
    void hasCorrectAbility() {
        SageOfLatNam card = new SageOfLatNam();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(2)
                .satisfies(effects -> {
                    assertThat(effects.get(0)).isInstanceOf(SacrificeArtifactCost.class);
                    assertThat(effects.get(1)).isInstanceOf(DrawCardEffect.class);
                });
    }

    // ===== Sacrifice cost =====

    @Test
    @DisplayName("Activating ability with one artifact auto-sacrifices it and puts ability on stack")
    void autoSacrificesOnlyArtifact() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent sage = findPermanent(player1, "Sage of Lat-Nam");
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating ability with multiple artifacts asks to choose which to sacrifice")
    void asksForChoiceWithMultipleArtifacts() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent sage = findPermanent(player1, "Sage of Lat-Nam");
        sage.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an artifact to sacrifice puts ability on stack")
    void choosingArtifactPutsAbilityOnStack() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent sage = findPermanent(player1, "Sage of Lat-Nam");
        sage.setSummoningSick(false);
        UUID scimitarId = findPermanent(player1, "Leonin Scimitar").getId();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, scimitarId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spellbook"));
    }

    // ===== Draw card on resolution =====

    @Test
    @DisplayName("Draws a card on ability resolution")
    void drawsCardOnResolution() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent sage = findPermanent(player1, "Sage of Lat-Nam");
        sage.setSummoningSick(false);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Activation restrictions =====

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new SageOfLatNam());

        Permanent sage = findPermanent(player1, "Sage of Lat-Nam");
        sage.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }

    @Test
    @DisplayName("Cannot activate ability when summoning sick (requires tap)")
    void cannotActivateWhenSummoningSick() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new SageOfLatNam());
        harness.addToBattlefield(player1, new LeoninScimitar());

        Permanent sage = findPermanent(player1, "Sage of Lat-Nam");
        sage.setSummoningSick(false);
        sage.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }
}
