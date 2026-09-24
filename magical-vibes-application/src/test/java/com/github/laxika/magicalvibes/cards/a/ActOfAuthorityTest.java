package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ActOfAuthority.class, GrizzlyBears.class, Ornithopter.class, AuraOfSilence.class})
class ActOfAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("On entering, may exile a target artifact")
    void etbMayExileArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castActOfAuthority();
        chooseTargetAndResolve(artifact.getId(), true);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Ornithopter");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ActOfAuthority);
    }

    @Test
    @DisplayName("Declining the entering ability leaves the target and enchantment in play")
    void decliningEtbLeavesTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        castActOfAuthority();
        chooseTargetAndResolve(artifact.getId(), false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("At upkeep, exiling a permanent gives its controller control of Act of Authority")
    void upkeepExileHandsOverControl() {
        Permanent act = harness.addToBattlefieldAndReturn(player1, new ActOfAuthority());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new AuraOfSilence());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Aura of Silence");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(act.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(act.getId()));
    }

    @Test
    @DisplayName("Only artifacts and enchantments are legal targets")
    void rejectsCreatureTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castActOfAuthority();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castActOfAuthority() {
        harness.setHand(player1, List.of(new ActOfAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTargetAndResolve(java.util.UUID targetId, boolean accept) {
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, accept);
    }
}
