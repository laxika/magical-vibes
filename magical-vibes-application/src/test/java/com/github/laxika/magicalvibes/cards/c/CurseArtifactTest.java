package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.f.FellwarStone;
import com.github.laxika.magicalvibes.cards.s.ScarwoodBandits;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseArtifact.class, FellwarStone.class, BogRats.class, ScarwoodBandits.class})
class CurseArtifactTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant an artifact")
    void canEnchantArtifact() {
        Permanent artifact = addArtifact(player2);

        harness.setHand(player1, List.of(new CurseArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.isAttached()
                        && p.getAttachedTo().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a non-artifact")
    void cannotEnchantNonArtifact() {
        addArtifact(player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BogRats());

        harness.setHand(player1, List.of(new CurseArtifact()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact");
    }

    @Test
    @DisplayName("Accepting the upkeep choice sacrifices the enchanted artifact")
    void acceptingChoiceSacrificesEnchantedArtifact() {
        Permanent artifact = addArtifact(player2);
        Permanent otherArtifact = addArtifact(player2);
        attachCurseArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);
        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
    }

    @Test
    @DisplayName("Accepting the choice still sacrifices the artifact if the Aura left first")
    void acceptingChoiceSacrificesArtifactAfterAuraLeaves() {
        Permanent artifact = addArtifact(player2);
        Permanent curse = attachCurseArtifact(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.inMutationScope(() ->
                assertThat(harness.getPermanentRemovalService().removePermanentToGraveyard(gd, curse)).isTrue());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    @DisplayName("Declining the upkeep choice deals 2 damage to the artifact's controller")
    void decliningChoiceDealsDamage() {
        Permanent artifact = addArtifact(player2);
        attachCurseArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals damage if the enchanted artifact left before the upkeep ability resolves")
    void dealsDamageWhenArtifactLeavesBeforeChoiceResolves() {
        Permanent artifact = addArtifact(player2);
        attachCurseArtifact(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.inMutationScope(() -> {
            assertThat(harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact)).isTrue();
            assertThat(harness.getPermanentRemovalService().removeOrphanedAuras(gd)).isTrue();
        });
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Deals damage if the artifact changes controller before the choice resolves")
    void dealsDamageWhenArtifactChangesControllerBeforeChoiceResolves() {
        Permanent artifact = addArtifact(player2);
        attachCurseArtifact(artifact);
        Permanent bandits = harness.addToBattlefieldAndReturn(player1, new ScarwoodBandits());
        bandits.setSummoningSick(false);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passPriority(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(bandits),
                null,
                artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent artifact = addArtifact(player2);
        attachCurseArtifact(artifact);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        harness.assertLife(player1, lifeBefore);
    }

    private Permanent attachCurseArtifact(Permanent artifact) {
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseArtifact());
        curse.setAttachedTo(artifact.getId());
        return curse;
    }

    private Permanent addArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new FellwarStone());
    }
}
