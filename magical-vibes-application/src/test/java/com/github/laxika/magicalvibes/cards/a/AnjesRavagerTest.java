package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnjesRavager.class, GrizzlyBears.class, RavensCrime.class})
class AnjesRavagerTest extends BaseCardTest {

    @Test
    void mustAttackWhenAble() {
        addCreatureReady(player1, new AnjesRavager());

        assertThatThrownBy(() -> declareAttackers(List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void attackingDiscardsHandAndDrawsThree() {
        addCreatureReady(player1, new AnjesRavager());
        GrizzlyBears firstDraw = new GrizzlyBears();
        GrizzlyBears secondDraw = new GrizzlyBears();
        GrizzlyBears thirdDraw = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void discardingOffersMadness() {
        AnjesRavager ravager = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(ravager.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    void acceptingMadnessCastsAnjesRavager() {
        AnjesRavager ravager = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ravager.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void tappedRavagerIsNotRequiredToAttack() {
        addCreatureReady(player1, new AnjesRavager()).setTapped(true);

        declareAttackers(List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSickRavagerIsNotRequiredToAttack() {
        harness.addToBattlefield(player1, new AnjesRavager());

        declareAttackers(List.of());

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void attackingWithEmptyHandStillDrawsThree() {
        addCreatureReady(player1, new AnjesRavager());
        AnjesRavager firstDraw = new AnjesRavager();
        AnjesRavager secondDraw = new AnjesRavager();
        AnjesRavager thirdDraw = new AnjesRavager();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void decliningMadnessPutsRavagerIntoGraveyard() {
        AnjesRavager ravager = discardViaRavensCrime();
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(ravager);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ravager);
        harness.assertNotOnBattlefield(player1, "Anje's Ravager");
    }

    @Test
    void attackDrawsBeforeDiscardedRavagersMadnessChoice() {
        addCreatureReady(player1, new AnjesRavager());
        AnjesRavager discarded = new AnjesRavager();
        AnjesRavager firstDraw = new AnjesRavager();
        AnjesRavager secondDraw = new AnjesRavager();
        AnjesRavager thirdDraw = new AnjesRavager();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, thirdDraw));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, thirdDraw);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstDraw, secondDraw, thirdDraw);
    }

    private AnjesRavager discardViaRavensCrime() {
        AnjesRavager ravager = new AnjesRavager();
        harness.setHand(player1, List.of(ravager));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return ravager;
    }
}
