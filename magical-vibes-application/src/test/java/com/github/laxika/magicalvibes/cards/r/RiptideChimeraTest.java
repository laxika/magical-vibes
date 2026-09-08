package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiptideChimeraTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Upkeep choice includes only enchantments the controller controls")
    void choiceIncludesOnlyControlledEnchantments() {
        Permanent chimera = addPermanent(player1, new RiptideChimera());
        Permanent anthem = addPermanent(player1, new GloriousAnthem());
        Permanent creature = addPermanent(player1, new GrizzlyBears());
        Permanent opponentAnthem = addPermanent(player2, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(chimera.getId(), anthem.getId())
                .doesNotContain(creature.getId(), opponentAnthem.getId());
    }

    @Test
    @DisplayName("Chosen enchantment is returned to its owner's hand")
    void chosenEnchantmentIsReturnedToHand() {
        addPermanent(player1, new RiptideChimera());
        Permanent anthem = addPermanent(player1, new GloriousAnthem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, anthem.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(anthem.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GloriousAnthem);
    }

    @Test
    @DisplayName("Can return itself when it is the only enchantment controlled")
    void canReturnItself() {
        RiptideChimera chimeraCard = new RiptideChimera();
        Permanent chimera = addPermanent(player1, chimeraCard);
        addPermanent(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(chimera.getId());
        harness.handlePermanentChosen(player1, chimera.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(chimera.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(chimeraCard);
    }
}
