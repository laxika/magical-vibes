package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InheritedFiend;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeirloomMirror.class, InheritedFiend.class, GrizzlyBears.class, Island.class})
class HeirloomMirrorTest extends BaseCardTest {

    @Test
    void activationDrawsMillsAndAddsRitualCounter() {
        Permanent mirror = addMirrorReady(player1);
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        Card milled = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).add(0, milled);
        gd.playerDecks.get(player1.getId()).add(0, drawn);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, mirror), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded, milled);
        assertThat(mirror.getCounterCount(CounterType.RITUAL)).isEqualTo(1);
    }

    @Test
    void transformsAfterTheThirdRitualCounter() {
        Permanent mirror = addMirrorReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        for (int i = 0; i < 6; i++) {
            gd.playerDecks.get(player1.getId()).add(0, new GrizzlyBears());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, indexOf(player1, mirror), null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            if (i < 2) {
                mirror.untap();
            }
        }

        assertThat(mirror.isTransformed()).isTrue();
        assertThat(mirror.getCounterCount(CounterType.RITUAL)).isZero();
    }

    @Test
    void backAbilityExilesCreatureAndAddsPlusOnePlusOneCounter() {
        Permanent fiend = addTransformedMirror(player1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, indexOf(player1, fiend), null, target.getId(), com.github.laxika.magicalvibes.model.Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(fiend.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void backAbilityCannotTargetNoncreatureCard() {
        Permanent fiend = addTransformedMirror(player1);
        Card target = new Island();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, fiend), null, target.getId(), com.github.laxika.magicalvibes.model.Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addMirrorReady(Player player) {
        Permanent mirror = new Permanent(new HeirloomMirror());
        mirror.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(mirror);
        return mirror;
    }

    private Permanent addTransformedMirror(Player player) {
        HeirloomMirror card = new HeirloomMirror();
        Permanent mirror = new Permanent(card);
        mirror.setSummoningSick(false);
        mirror.setCard(card.getBackFaceCard());
        mirror.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(mirror);
        return mirror;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
