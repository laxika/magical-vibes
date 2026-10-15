package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalefulStrix;
import com.github.laxika.magicalvibes.cards.s.ShadowMysteriousAssassin;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InterceptorShadowsHound.class, ShadowMysteriousAssassin.class, BalefulStrix.class,
        SwordsToPlowshares.class})
class InterceptorShadowsHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Assassins menace, but not other creatures or opponents' Assassins")
    void givesControlledAssassinsMenace() {
        harness.addToBattlefield(player1, new InterceptorShadowsHound());
        Permanent ownAssassin = harness.addToBattlefieldAndReturn(player1, new ShadowMysteriousAssassin());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new BalefulStrix());
        Permanent opponentAssassin = harness.addToBattlefieldAndReturn(player2, new ShadowMysteriousAssassin());

        assertThat(gqs.hasKeyword(gd, ownAssassin, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAssassin, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Triggers from the graveyard when one or more legendary creatures attack")
    void triggersForLegendaryAttack() {
        addCreatureReady(player1, new ShadowMysteriousAssassin());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{2}{B}");
    }

    @Test
    @DisplayName("Does not trigger from the graveyard for a nonlegendary attack")
    void doesNotTriggerForNonlegendaryAttack() {
        addCreatureReady(player1, new BalefulStrix());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));

        declareAttackers(List.of(0));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Interceptor, Shadow's Hound"));
    }

    @Test
    @DisplayName("Returns tapped and attacking after paying {2}{B}")
    void returnsTappedAndAttacking() {
        addCreatureReady(player1, new ShadowMysteriousAssassin());
        InterceptorShadowsHound hound = new InterceptorShadowsHound();
        harness.setGraveyard(player1, List.of(hound));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(hound.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttacking()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(hound.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canDeclinePayment() {
        addCreatureReady(player1, new ShadowMysteriousAssassin());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Interceptor, Shadow's Hound");
        assertThat(countPermanents(player1, "Interceptor, Shadow's Hound")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void triggersOnceForMultipleLegendaryAttackers() {
        addCreatureReady(player1, new ShadowMysteriousAssassin());
        addCreatureReady(player1, new InterceptorShadowsHound());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));

        declareAttackers(List.of(0, 1));

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Interceptor, Shadow's Hound")))
                .hasSize(1);
    }

    @Test
    void stillOffersPaymentAfterLegendaryAttackerLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new ShadowMysteriousAssassin());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        declareAttackers(List.of(0));
        harness.castInstant(player2, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(countPermanents(player1, "Interceptor, Shadow's Hound")).isEqualTo(1);
    }

    @Test
    void doesNotTriggerForOpponentsLegendaryAttack() {
        addCreatureReady(player2, new ShadowMysteriousAssassin());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    void onlyReturnsItselfFromGraveyard() {
        addCreatureReady(player1, new ShadowMysteriousAssassin());
        InterceptorShadowsHound hound = new InterceptorShadowsHound();
        BalefulStrix other = new BalefulStrix();
        harness.setGraveyard(player1, List.of(hound, other));
        harness.addMana(player1, ManaColor.BLACK, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Interceptor, Shadow's Hound")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getId()).isEqualTo(other.getId());
    }

    @Test
    void assassinsLoseGrantedMenaceWhenHoundLeaves() {
        Permanent hound = harness.addToBattlefieldAndReturn(player1, new InterceptorShadowsHound());
        Permanent assassin = harness.addToBattlefieldAndReturn(player1, new ShadowMysteriousAssassin());
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.MENACE)).isTrue();

        harness.castInstant(player2, 0, hound.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, assassin, Keyword.MENACE)).isFalse();
    }
}
