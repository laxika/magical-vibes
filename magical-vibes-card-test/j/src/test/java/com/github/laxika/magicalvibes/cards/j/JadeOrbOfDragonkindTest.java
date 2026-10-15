package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BatheInGold;
import com.github.laxika.magicalvibes.cards.d.DragonbornLooter;
import com.github.laxika.magicalvibes.cards.h.HobgoblinCaptain;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.y.YoungRedDragon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JadeOrbOfDragonkind.class, HobgoblinCaptain.class, DragonbornLooter.class,
        Naturalize.class, YoungRedDragon.class, BatheInGold.class})
class JadeOrbOfDragonkindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Jade Orb of Dragonkind adds green mana")
    void tappingAddsGreenMana() {
        Permanent orb = addReadyOrb();

        harness.activateAbility(player1, 0, null, null);

        assertThat(manaPool().get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(orb.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana spent on a Dragon creature spell grants a counter and hexproof")
    void dragonCastWithOrbManaGainsCounterAndHexproof() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new DragonbornLooter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Dragonborn Looter");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Mana spent on a non-Dragon creature spell does not grant the bonus")
    void nonDragonCastWithOrbManaDoesNotGainBonus() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new HobgoblinCaptain()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent creature = findPermanent(player1, "Hobgoblin Captain");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A Dragon cast with mana from another source does not get the bonus")
    void dragonCastWithOtherManaDoesNotGainBonus() {
        addReadyOrb();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new DragonbornLooter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Dragonborn Looter");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The Dragon has hexproof immediately upon entering, before any entry triggers resolve")
    void hexproofAppliesImmediatelyOnEntry() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new DragonbornLooter()));

        harness.castCreature(player1, 0);
        while (gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().getName().equals("Dragonborn Looter"))) {
            assertThat(gd.stack).isNotEmpty();
            harness.passBothPriorities();
        }

        Permanent dragon = findPermanent(player1, "Dragonborn Looter");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The bonus survives the Orb being destroyed before its mana is spent")
    void manaRetainsBonusAfterOrbLeavesBattlefield() {
        Permanent orb = addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, orb.getId());
        harness.assertNotOnBattlefield(player1, "Jade Orb of Dragonkind");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new DragonbornLooter()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Dragonborn Looter");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Each mana spent from the same Orb grants an additional counter")
    void twoManaFromSameOrbGrantTwoCounters() {
        Permanent orb = addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        orb.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player1, List.of(new YoungRedDragon()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Young Red Dragon");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();
        assertThat(manaPool().getTotal()).isZero();
    }

    @Test
    @DisplayName("Hexproof lasts through the opponent's turn and expires on your next turn; the counter remains")
    void hexproofExpiresButCounterRemains() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new DragonbornLooter()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent dragon = findPermanent(player1, "Dragonborn Looter");

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, dragon, Keyword.HEXPROOF)).isFalse();
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addReadyOrb() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new JadeOrbOfDragonkind());
        orb.setSummoningSick(false);
        return orb;
    }

    private ManaPool manaPool() {
        return gd.playerManaPools.get(player1.getId());
    }

}
