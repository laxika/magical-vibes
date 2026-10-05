package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightExemplar.class, BlackKnight.class, GrizzlyBears.class, WrathOfGod.class,
        LightningBolt.class, Unsummon.class})
class KnightExemplarTest extends BaseCardTest {

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

        List<Permanent> exemplars = findPermanents(player1, "Knight Exemplar");

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

        harness.castFromHand(player1, new KnightExemplar(), "{1}{W}{W}");
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
    @DisplayName("Indestructible Knight survives simultaneous destruction of its Exemplar")
    void indestructibleKnightSurvivesSimultaneousDestruction() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());

        Permanent knight = findPermanent(player1, "Black Knight");
        assertThat(gqs.hasKeyword(gd, knight, Keyword.INDESTRUCTIBLE)).isTrue();

        // Cast Wrath of God to try to destroy everything
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        // Black Knight should survive (indestructible from Knight Exemplar)
        harness.assertOnBattlefield(player1, "Black Knight");

        // Knight Exemplar should be destroyed (doesn't buff itself)
        harness.assertNotOnBattlefield(player1, "Knight Exemplar");
    }

    @Test
    @DisplayName("Two Knight Exemplars make all Knights survive Wrath of God")
    void twoExemplarsMakeAllKnightsSurviveWrath() {
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new KnightExemplar());
        harness.addToBattlefield(player1, new BlackKnight());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Cast Wrath of God
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        // Both Knight Exemplars buff each other → both indestructible → both survive
        assertThat(countPermanents(player1, "Knight Exemplar")).isEqualTo(2);

        // Black Knight survives too
        harness.assertOnBattlefield(player1, "Black Knight");

        // Opponent's non-Knight creature is destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible does not prevent a Knight from being returned to hand")
    void indestructibleKnightCanBeBounced() {
        harness.addToBattlefield(player1, new KnightExemplar());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Knight Exemplar");
        harness.assertNotOnBattlefield(player1, "Black Knight");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof BlackKnight);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Knight survives lethal damage but dies when its Exemplar is bounced")
    void lethalDamageBecomesFatalWhenExemplarLeaves() {
        Permanent exemplar = harness.addToBattlefieldAndReturn(player1, new KnightExemplar());
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new BlackKnight());

        harness.setHand(player2, List.of(new LightningBolt(), new Unsummon()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, knight.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Knight");
        assertThat(knight.getMarkedDamage()).isEqualTo(3);

        harness.castInstant(player2, 0, exemplar.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knight Exemplar");
        harness.assertNotOnBattlefield(player1, "Black Knight");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BlackKnight);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof KnightExemplar);
    }
}
