package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LeonardoDaVinci;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArnoDorian.class, AssassinInitiate.class, LeonardoDaVinci.class, RoyalAssassin.class, SoulSummons.class})
class ArnoDorianTest extends BaseCardTest {

    @Test
    @DisplayName("Buffs other Assassins you control")
    void buffsOtherAssassinsYouControl() {
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        int basePower = gqs.getEffectivePower(gd, assassin);
        int baseToughness = gqs.getEffectiveToughness(gd, assassin);

        addCreatureReady(player1, new ArnoDorian());

        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, assassin)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent arno = new Permanent(new ArnoDorian());
        int basePower = gqs.getEffectivePower(gd, arno);
        int baseToughness = gqs.getEffectiveToughness(gd, arno);
        gd.playerBattlefields.get(player1.getId()).add(arno);

        addCreatureReady(player1, new AssassinInitiate());

        assertThat(gqs.getEffectivePower(gd, arno)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, arno)).isEqualTo(baseToughness);
    }

    @Test
    @DisplayName("Does not buff non-Assassins or opposing Assassins")
    void doesNotBuffNonAssassinsOrOpposingAssassins() {
        Permanent nonAssassin = addCreatureReady(player1, new LeonardoDaVinci());
        Permanent opposingAssassin = addCreatureReady(player2, new AssassinInitiate());
        int nonAssassinPower = gqs.getEffectivePower(gd, nonAssassin);
        int nonAssassinToughness = gqs.getEffectiveToughness(gd, nonAssassin);
        int opposingPower = gqs.getEffectivePower(gd, opposingAssassin);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingAssassin);

        addCreatureReady(player1, new ArnoDorian());

        assertThat(gqs.getEffectivePower(gd, nonAssassin)).isEqualTo(nonAssassinPower);
        assertThat(gqs.getEffectiveToughness(gd, nonAssassin)).isEqualTo(nonAssassinToughness);
        assertThat(gqs.getEffectivePower(gd, opposingAssassin)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingAssassin)).isEqualTo(opposingToughness);
    }

    @Test
    @DisplayName("Can be cast face down and turned face up for its disguise cost")
    void canBeDisguisedAndTurnedFaceUp() {
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        int basePower = gqs.getEffectivePower(gd, assassin);
        ArnoDorian card = new ArnoDorian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent arno = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(arno.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, arno)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, arno)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, arno, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(basePower);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(arno));

        assertThat(arno.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, arno, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(basePower + 2);
    }

    @Test
    @DisplayName("Disguise ward counters an opposing ability when its controller cannot pay")
    void disguiseWardCountersOpposingAbility() {
        addCreatureReady(player2, new RoyalAssassin());
        harness.setHand(player1, List.of(new ArnoDorian()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent arno = gd.playerBattlefields.get(player1.getId()).getFirst();
        arno.tap();

        harness.activateAbility(player2, 0, null, arno.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(arno);
    }

    @Test
    @DisplayName("Manifesting Arno does not give it disguise ward")
    void manifestedArnoDoesNotHaveWard() {
        addCreatureReady(player2, new RoyalAssassin());
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.setLibrary(player1, List.of(new ArnoDorian()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        Permanent arno = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(arno.isManifested()).isTrue();
        assertThat(gqs.hasKeyword(gd, arno, Keyword.WARD)).isFalse();
        arno.tap();

        harness.activateAbility(player2, 0, null, arno.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(arno);
        harness.assertInGraveyard(player1, "Arno Dorian");
    }
    @Test
    @DisplayName("The Assassin boost ends when Arno leaves the battlefield")
    void boostEndsWhenArnoLeaves() {
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        int basePower = gqs.getEffectivePower(gd, assassin);
        Permanent arno = addCreatureReady(player1, new ArnoDorian());
        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(basePower + 2);

        gd.playerBattlefields.get(player1.getId()).remove(arno);
        gd.playerGraveyards.get(player1.getId()).add(arno.getOriginalCard());

        assertThat(gqs.getEffectivePower(gd, assassin)).isEqualTo(basePower);
    }
}
