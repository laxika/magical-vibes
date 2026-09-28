package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RatonhnhakTon;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({InterceptorShadowsHound.class, RatonhnhakTon.class, GrizzlyBears.class})
class InterceptorShadowsHoundTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Assassins menace, but not other creatures or opponents' Assassins")
    void givesControlledAssassinsMenace() {
        harness.addToBattlefield(player1, new InterceptorShadowsHound());
        Permanent ownAssassin = harness.addToBattlefieldAndReturn(player1, new RatonhnhakTon());
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentAssassin = harness.addToBattlefieldAndReturn(player2, new RatonhnhakTon());

        assertThat(gqs.hasKeyword(gd, ownAssassin, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAssassin, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Triggers from the graveyard when one or more legendary creatures attack")
    void triggersForLegendaryAttack() {
        addReady(player1, new InterceptorShadowsHound());
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
        addReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new InterceptorShadowsHound()));

        declareAttackers(List.of(0));

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Interceptor, Shadow's Hound"));
    }

    @Test
    @DisplayName("Returns tapped and attacking after paying {2}{B}")
    void returnsTappedAndAttacking() {
        addReady(player1, new InterceptorShadowsHound());
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
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(hound.getId()));
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
