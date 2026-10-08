package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AleshasVanguard;
import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WildSlash.class, Card.class, AleshasVanguard.class, FeralKrushok.class})
class WildSlashTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage that can be prevented without ferocious")
    void damageCanBePreventedWithoutFerocious() {
        addCreature(player1, 3, 3);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        castWildSlash();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(8);
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    @Test
    @DisplayName("With ferocious, damage cannot be prevented this turn")
    void damageCannotBePreventedWithFerocious() {
        addCreature(player1, 4, 4);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        castWildSlash();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
        assertThat(gd.damageCantBePreventedThisTurn).isTrue();
    }

    @Test
    @DisplayName("Checks ferocious as Wild Slash resolves")
    void checksFerociousAtResolution() {
        addCreature(player1, 4, 4);
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(8);
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    private void castWildSlash() {
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    @Test
    void dealsTwoDamageWithoutCreatures() {
        castWildSlash();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Wild Slash");
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    @Test
    void dealsTwoDamageToCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new AleshasVanguard());
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Alesha's Vanguard");
    }

    @Test
    void opponentsPowerfulCreatureDoesNotEnableFerocious() {
        harness.addToBattlefield(player2, new FeralKrushok());
        gd.playerDamagePreventionShields.put(player2.getId(), 10);

        castWildSlash();

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(8);
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    @Test
    void gainingFerociousBeforeResolutionStopsPrevention() {
        gd.playerDamagePreventionShields.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new FeralKrushok());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(10);
    }

    @Test
    void illegalTargetPreventsFerociousEffectFromResolving() {
        harness.addToBattlefield(player1, new FeralKrushok());
        var target = harness.addToBattlefieldAndReturn(player2, new AleshasVanguard());
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
        harness.assertInGraveyard(player1, "Wild Slash");
    }

    @Test
    void ferociousAppliesToLaterOpponentsDamageAfterCreatureLeaves() {
        var creature = harness.addToBattlefieldAndReturn(player1, new FeralKrushok());
        castWildSlash();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        harness.setHand(player2, List.of(new WildSlash()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(10);
    }

    @Test
    void ferociousExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new FeralKrushok());
        castWildSlash();
        harness.passUntil(player2, TurnStep.UPKEEP);
        gd.playerDamagePreventionShields.put(player1.getId(), 10);
        harness.setHand(player2, List.of(new WildSlash()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields.get(player1.getId())).isEqualTo(8);
        assertThat(gd.damageCantBePreventedThisTurn).isFalse();
    }

    private void addCreature(com.github.laxika.magicalvibes.model.Player player, int power, int toughness) {
        Card creature = new Card();
        creature.setName("Creature");
        creature.setType(CardType.CREATURE);
        creature.setPower(power);
        creature.setToughness(toughness);
        harness.addToBattlefield(player, creature);
    }
}
