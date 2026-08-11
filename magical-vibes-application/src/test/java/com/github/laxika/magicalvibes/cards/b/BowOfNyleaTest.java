package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BowOfNyleaTest extends BaseCardTest {

    private Permanent addBow() {
        Permanent bow = harness.addToBattlefieldAndReturn(player1, new BowOfNylea());
        harness.addMana(player1, ManaColor.GREEN, 2);
        return bow;
    }

    private int bowIndex(Permanent bow) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(bow);
    }

    @Test
    @DisplayName("Grants deathtouch to your attacking creatures only")
    void grantsDeathtouchToAttackingCreaturesYouControl() {
        addBow();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on target creature")
    void putsCounterOnTargetCreature() {
        Permanent bow = addBow();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, bowIndex(bow), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 2 damage only to target creature with flying")
    void damagesTargetCreatureWithFlying() {
        Permanent bow = addBow();
        Permanent flyer = addCreatureReady(player2, new AirElemental());

        harness.activateAbility(player1, bowIndex(bow), 1, null, flyer.getId());
        harness.passBothPriorities();

        assertThat(flyer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a nonflying creature for the damage mode")
    void rejectsNonflyingDamageTarget() {
        Permanent bow = addBow();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, bowIndex(bow), 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gains 3 life")
    void gainsLife() {
        Permanent bow = addBow();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, bowIndex(bow), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Puts up to four targeted graveyard cards on the bottom in the chosen order")
    void putsGraveyardCardsOnBottomInChosenOrder() {
        Permanent bow = addBow();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second)));
        Card libraryCard = new AirElemental();
        harness.setLibrary(player1, new ArrayList<>(List.of(libraryCard)));

        harness.activateAbilityWithGraveyardTargets(player1, bowIndex(bow), 3,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(libraryCard.getId(), first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent bow = addBow();
        Card card = new Forest();
        harness.setGraveyard(player2, List.of(card));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, bowIndex(bow), 3, List.of(card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
