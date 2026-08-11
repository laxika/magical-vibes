package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.cards.r.Revitalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AjaniStrengthOfThePrideTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gains life for controlled creatures and planeswalkers")
    void plusOneGainsLifeForCreaturesAndPlaneswalkers() {
        Permanent ajani = addReadyAjani(player1, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addReadyChandra(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("-2 creates an Ajani's Pridemate token that grows when its controller gains life")
    void minusTwoCreatesPridemateThatGrowsOnLifeGain() {
        addReadyAjani(player1, 4);
        harness.setLife(player1, 17);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent pridemate = findPermanentByName(player1, "Ajani's Pridemate");
        assertThat(pridemate.getEffectivePower()).isEqualTo(2);
        assertThat(pridemate.getEffectiveToughness()).isEqualTo(2);

        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Revitalize()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(pridemate.getEffectivePower()).isEqualTo(3);
        assertThat(pridemate.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("0 exiles Ajani and opponents' artifacts and creatures at the life threshold")
    void zeroExilesAjaniAndOpponentsArtifactsAndCreatures() {
        Permanent ajani = addReadyAjani(player1, 4);
        harness.setLife(player1, 35);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LiquimetalCoating());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Grizzly Bears", "Liquimetal Coating");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Ajani, Strength of the Pride");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Liquimetal Coating");
        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("0 does nothing below 15 life above starting life")
    void zeroDoesNothingBelowLifeThreshold() {
        Permanent ajani = addReadyAjani(player1, 4);
        harness.setLife(player1, 34);
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ajani);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(p -> p.getCard().getName())
                .containsExactly("Grizzly Bears");
    }

    private Permanent addReadyAjani(Player player, int loyalty) {
        Permanent perm = new Permanent(new AjaniStrengthOfThePride());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = new Permanent(new ChandraNalaar());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanentByName(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
