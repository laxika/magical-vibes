package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WickerfolkThresher.class, Forest.class, GrizzlyBears.class, Shock.class,
        LeoninScimitar.class, Pacifism.class})
class WickerfolkThresherTest extends BaseCardTest {

    @Test
    @DisplayName("With delirium, its attack trigger may put a top land onto the battlefield")
    void deliriousAttackPutsLandOntoBattlefield() {
        addReadyThresher();
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        setDelirium();

        declareAttack();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(topLand)).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topLand);
    }

    @Test
    @DisplayName("Declining the delirious attack trigger puts a top land into hand")
    void decliningLandPutsItIntoHand() {
        addReadyThresher();
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        setDelirium();

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(findPermanent(topLand)).isNull();
    }

    @Test
    @DisplayName("A nonland top card goes into hand without a may choice")
    void nonlandTopCardGoesIntoHand() {
        addReadyThresher();
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        setDelirium();

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Without delirium, attacking does not trigger the ability")
    void doesNotTriggerWithoutDelirium() {
        addReadyThresher();
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new LeoninScimitar()));

        declareAttack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topLand);
    }

    private void addReadyThresher() {
        addCreatureReady(player1, new WickerfolkThresher());
    }

    private void declareAttack() {
        declareAttackers(List.of(0));
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Pacifism()));
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
    }
}
