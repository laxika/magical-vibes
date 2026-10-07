package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThassaGodOfTheSea.class, FugitiveWizard.class, GrizzlyBears.class, MycosynthLattice.class})
class ThassaGodOfTheSeaTest extends BaseCardTest {

    @Test
    @DisplayName("Thassa is not a creature below five devotion to blue")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent thassa = addThassa();
        addBluePermanents(3);

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa becomes a creature at five devotion to blue")
    void becomesCreatureAtDevotionThreshold() {
        Permanent thassa = addThassa();
        addBluePermanents(4);

        assertThat(gqs.isCreature(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa scries 1 at the beginning of its controller's upkeep")
    void scriesAtUpkeep() {
        addThassa();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Thassa makes a creature you control unblockable until end of turn")
    void makesOwnCreatureUnblockable() {
        addThassa();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Thassa cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        addThassa();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Opponent's blue permanents do not contribute to Thassa's devotion")
    void ignoresOpponentsDevotion() {
        Permanent thassa = addThassa();
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new FugitiveWizard());
        }

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
    }

    @Test
    @DisplayName("Thassa stops being a creature immediately when devotion drops")
    void stopsBeingCreatureWhenDevotionDrops() {
        Permanent thassa = addThassa();
        addBluePermanents(4);
        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        assertThat(gqs.isCreature(gd, thassa)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(wizard);
        gd.playerGraveyards.get(player1.getId()).add(wizard.getCard());

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa does not scry during the opponent's upkeep")
    void doesNotScryAtOpponentsUpkeep() {
        addThassa();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Thassa's scry can put exactly the top card on the bottom")
    void scriesTopCardToBottom() {
        addThassa();
        FugitiveWizard top = new FugitiveWizard();
        GrizzlyBears next = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, next));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
    }

    @Test
    @DisplayName("Thassa's unblockable effect expires after the turn")
    void unblockableExpiresAfterTurn() {
        addThassa();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasCantBeBlocked(gd, creature)).isTrue();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Losing the creature type preserves artifact type granted by an earlier Lattice")
    void preservesOtherCardTypesBelowDevotionThreshold() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent thassa = addThassa();

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.isEnchantment(gd, thassa)).isTrue();
        assertThat(gqs.isArtifact(gd, thassa)).isTrue();
    }

    @Test
    @DisplayName("Thassa cannot target itself while it is not a creature")
    void cannotTargetNoncreatureThassa() {
        Permanent thassa = addThassa();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, thassa.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An ability targeting Thassa does not resolve if devotion drops below five")
    void targetBecomingNoncreatureIsIllegalOnResolution() {
        Permanent thassa = addThassa();
        addBluePermanents(4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, thassa.getId());
        Permanent wizard = findPermanent(player1, "Fugitive Wizard");
        gd.playerBattlefields.get(player1.getId()).remove(wizard);
        gd.playerGraveyards.get(player1.getId()).add(wizard.getCard());

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, thassa)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, thassa)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addThassa() {
        return harness.addToBattlefieldAndReturn(player1, new ThassaGodOfTheSea());
    }

    private void addBluePermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new FugitiveWizard());
        }
    }
}
