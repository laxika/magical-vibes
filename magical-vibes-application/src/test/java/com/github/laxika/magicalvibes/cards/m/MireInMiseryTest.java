package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MireInMisery.class, GloriousAnthem.class, GrizzlyBears.class, FountainOfYouth.class})
class MireInMiseryTest extends BaseCardTest {

    @Test
    void eachOpponentSacrificesACreatureOrEnchantmentOfTheirChoice() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(creature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(enchantment, artifact);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void doesNothingWhenOpponentControlsNoCreatureOrEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
    }

    private void cast() {
        harness.setHand(player1, List.of(new MireInMisery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
