package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KnightExemplarTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Knight Exemplar has static boost effect for Knights with indestructible")
    void hasCorrectStaticEffect() {
        KnightExemplar card = new KnightExemplar();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(StaticBoostEffect.class);

        StaticBoostEffect effect = (StaticBoostEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).containsExactly(Keyword.INDESTRUCTIBLE);
        assertThat(effect.scope()).isEqualTo(GrantScope.OWN_CREATURES);
        assertThat(effect.filter()).isInstanceOf(PermanentHasAnySubtypePredicate.class);
    }

    // ===== Static effect: buffs other Knights you control =====

    @Test
    @DisplayName("Other Knight creatures you control get +1/+1 and indestructible")
    void buffsOtherKnightsYouControl() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Knight Exemplar does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new KnightExemplar());

        Permanent exemplar = findPermanent(player1, "Knight Exemplar");

        assertThat(gqs.getEffectivePower(gd, exemplar)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, exemplar)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, exemplar, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff non-Knight creatures")
    void doesNotBuffNonKnights() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Does not buff opponent's Knight creatures")
    void doesNotBuffOpponentKnights() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player2, new BlackKnight());

        Permanent opponentKnight = findPermanent(player2, "Black Knight");

        assertThat(gqs.getEffectivePower(gd, opponentKnight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKnight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentKnight, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    // ===== Multiple Knight Exemplars =====

    @Test
    @DisplayName("Two Knight Exemplars buff each other")
    void twoExemplarsBuffEachOther() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new KnightExemplar());

        List<Permanent> exemplars = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight Exemplar"))
                .toList();

        assertThat(exemplars).hasSize(2);
        for (Permanent exemplar : exemplars) {
            assertThat(gqs.getEffectivePower(gd, exemplar)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, exemplar)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, exemplar, Keyword.INDESTRUCTIBLE)).isTrue();
        }
    }

    @Test
    @DisplayName("Two Knight Exemplars give +2/+2 and indestructible to other Knights")
    void twoExemplarsStackBonuses() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");

        // 2/2 base + 2/2 from two exemplars = 4/4
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Knight Exemplar leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Knight Exemplar"));

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Bonus applies when Knight Exemplar resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.setHand(player1, List.of(new KnightExemplar()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");

        knight.setPowerModifier(knight.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(8); // 2 base + 5 spell + 1 static

        knight.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    // ===== Indestructible prevents destruction =====

    @Test
    @DisplayName("Indestructible Knight survives targeted destroy effect")
    void indestructibleKnightSurvivesTargetedDestroy() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Wrath of God to try to destroy everything
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Black Knight should survive (indestructible from Knight Exemplar)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));

        // Knight Exemplar should be destroyed (doesn't buff itself)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Knight Exemplar"));
    }

    @Test
    @DisplayName("Two Knight Exemplars make all Knights survive Wrath of God")
    void twoExemplarsMakeAllKnightsSurviveWrath() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Cast Wrath of God
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        // Both Knight Exemplars buff each other → both indestructible → both survive
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Knight Exemplar"))
                .count()).isEqualTo(2);

        // Black Knight survives too
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Black Knight"));

        // Opponent's non-Knight creature is destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Helper methods =====

}
