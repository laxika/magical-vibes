package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamiOfMourning.class, GrizzlyBears.class, HillGiant.class})
class KamiOfMourningTest extends BaseCardTest {

    @Test
    @DisplayName("Targets a creature you control or a creature card in your graveyard")
    void offersOnlyControlledCreatureZones() {
        Permanent controlledCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card controlledGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(controlledGraveyardCreature));
        harness.setGraveyard(player2, List.of(opponentGraveyardCreature));

        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(controlledCreature.getId());
        assertThat(choice.validIds()).doesNotContain(opponentCreature.getId());
        assertThat(choice.validCardIds()).containsExactly(controlledGraveyardCreature.getId());
        assertThat(choice.validCardIds()).doesNotContain(opponentGraveyardCreature.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .hasMessageContaining("Too few targets");
    }

    @Test
    void returnsASelectedGraveyardCreatureAfterAGreaterManaValueCreatureDies() {
        Card targetCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(targetCard));
        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(targetCard.getId()));
        resolveAllTriggers();

        Permanent sameManaValueCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        kill(sameManaValueCreature);
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(targetCard);

        Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        kill(greaterManaValueCreature);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(targetCard.getId())
                        && permanent.isTapped());
    }

    @Test
    void aBattlefieldTargetKeepsTheAbilityAfterItDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new KamiOfMourning());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();

        kill(target);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getOriginalCard().getId()));

        Permanent greaterManaValueCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        kill(greaterManaValueCreature);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(target.getOriginalCard().getId())
                        && permanent.isTapped());
    }

    private void kill(Permanent permanent) {
        permanent.setMarkedDamage(gqs.getEffectiveToughness(gd, permanent));
        harness.runStateBasedActions();
    }
}
