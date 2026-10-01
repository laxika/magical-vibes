package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShadowKin.class, Forest.class, GrizzlyBears.class})
class ShadowKinTest extends BaseCardTest {

    @Test
    void millsEachPlayerAndCopiesAChosenMilledCreatureWhileRetainingItsAbility() {
        Permanent shadowKin = harness.addToBattlefieldAndReturn(player1, new ShadowKin());
        Card milledCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(milledCreature, new Forest(), new Forest()));

        resolveUpkeepTrigger();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3).contains(milledCreature);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(milledCreature);
        assertThat(shadowKin.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(shadowKin.getCard().getPower()).isEqualTo(2);
        assertThat(shadowKin.getCard().getToughness()).isEqualTo(2);
        assertThat(shadowKin.getCard().getEffectRegistrations(EffectSlot.UPKEEP_TRIGGERED))
                .isNotEmpty();
    }

    @Test
    void decliningCopyLeavesMilledCardsInGraveyards() {
        Permanent shadowKin = harness.addToBattlefieldAndReturn(player1, new ShadowKin());
        Card milledCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(milledCreature, new Forest(), new Forest()));

        resolveUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(shadowKin.getCard().getName()).isEqualTo("Shadow Kin");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void doesNotOfferCopyChoiceWhenNoCreatureWasMilled() {
        harness.addToBattlefield(player1, new ShadowKin());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        resolveUpkeepTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    private void resolveUpkeepTrigger() {
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
