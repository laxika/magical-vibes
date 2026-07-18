package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RelicBindTest extends BaseCardTest {

    // "Enchant artifact an opponent controls. Whenever enchanted artifact becomes tapped, choose
    //  one — this Aura deals 1 damage to target player or planeswalker; or target player gains 1 life."

    // ===== Casting and targeting =====

    @Test
    @DisplayName("Can cast Relic Bind targeting an artifact an opponent controls")
    void canTargetOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, java.util.List.of(new RelicBind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(artifact.getId());
    }

    @Test
    @DisplayName("Cannot cast Relic Bind targeting an artifact you control")
    void cannotTargetOwnArtifact() {
        harness.addToBattlefield(player2, new Ornithopter()); // valid target so the spell is playable
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, java.util.List.of(new RelicBind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    @DisplayName("Cannot cast Relic Bind targeting a non-artifact")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player2, new Ornithopter()); // valid target so the spell is playable
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, java.util.List.of(new RelicBind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact an opponent controls");
    }

    @Test
    @DisplayName("Resolving Relic Bind attaches it to the target artifact")
    void resolvingAttachesToArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        harness.setHand(player1, java.util.List.of(new RelicBind()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Relic Bind")
                        && artifact.getId().equals(p.getAttachedTo()));
    }

    // ===== Tap trigger: modal ability =====

    @Test
    @DisplayName("Damage mode deals 1 damage to the chosen player when the enchanted artifact taps")
    void damageModeDealsOne() {
        Permanent artifact = attachAuraToOpponentArtifact();
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        tapAndResolveModal(artifact, ChoiceContext.RelicBindModeChoice.DAMAGE, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before - 1);
    }

    @Test
    @DisplayName("Damage mode may target the Aura's controller")
    void damageModeCanTargetController() {
        Permanent artifact = attachAuraToOpponentArtifact();
        int p1Before = gd.playerLifeTotals.get(player1.getId());

        tapAndResolveModal(artifact, ChoiceContext.RelicBindModeChoice.DAMAGE, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before - 1);
    }

    @Test
    @DisplayName("Life mode makes the chosen player gain 1 life when the enchanted artifact taps")
    void lifeModeGainsOne() {
        Permanent artifact = attachAuraToOpponentArtifact();
        int p1Before = gd.playerLifeTotals.get(player1.getId());

        tapAndResolveModal(artifact, ChoiceContext.RelicBindModeChoice.LIFE, player1.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Before + 1);
    }

    @Test
    @DisplayName("An artifact with no Relic Bind attached does not trigger")
    void unenchantedArtifactDoesNotTrigger() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        int p2Before = gd.playerLifeTotals.get(player2.getId());

        artifact.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, artifact);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Before);
    }

    // ===== Helpers =====

    private Permanent attachAuraToOpponentArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent aura = new Permanent(new RelicBind());
        aura.setAttachedTo(artifact.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return artifact;
    }

    private void tapAndResolveModal(Permanent artifact, String mode, UUID targetId) {
        artifact.tap();
        harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, artifact);
        harness.getStackResolutionService().resolveTopOfStack(gd); // resolve trigger -> mode prompt
        harness.handleListChoice(player1, mode);                    // choose mode -> target prompt
        harness.handlePermanentChosen(player1, targetId);           // choose target -> apply
    }
}
