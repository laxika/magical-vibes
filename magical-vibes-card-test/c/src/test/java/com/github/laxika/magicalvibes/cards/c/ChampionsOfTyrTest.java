package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChampionsOfTyr.class, SteadfastPaladin.class})
class ChampionsOfTyrTest extends BaseCardTest {

    @Test
    void choosesPlusOneCounterForNextCreatureSpell() {
        SteadfastPaladin paladin = castChampionsThenCreature();

        harness.handleListChoice(player1, "Put a +1/+1 counter on that creature");
        resolveAllTriggers();

        Permanent permanent = findPermanent(paladin.getId());
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(permanent.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(permanent.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    void choosesFlyingCounterForNextCreatureSpell() {
        SteadfastPaladin paladin = castChampionsThenCreature();

        harness.handleListChoice(player1, "Put a flying counter on that creature");
        resolveAllTriggers();

        Permanent permanent = findPermanent(paladin.getId());
        assertThat(permanent.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.FLYING)).isTrue();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void choosesLifelinkCounterForNextCreatureSpell() {
        SteadfastPaladin paladin = castChampionsThenCreature();

        harness.handleListChoice(player1, "Put a lifelink counter on that creature");
        resolveAllTriggers();

        Permanent permanent = findPermanent(paladin.getId());
        assertThat(permanent.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.LIFELINK)).isTrue();
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void boonIsConsumedByOnlyOneCreatureSpell() {
        ChampionsOfTyr champions = new ChampionsOfTyr();
        SteadfastPaladin firstPaladin = new SteadfastPaladin();
        SteadfastPaladin secondPaladin = new SteadfastPaladin();
        harness.setHand(player1, List.of(champions, firstPaladin));
        harness.addMana(player1, ManaColor.WHITE, 8);


        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.setHand(player1, List.of(firstPaladin));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a +1/+1 counter on that creature");
        resolveAllTriggers();

        harness.setHand(player1, List.of(secondPaladin));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanent(firstPaladin.getId()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(findPermanent(secondPaladin.getId()).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    private SteadfastPaladin castChampionsThenCreature() {
        ChampionsOfTyr champions = new ChampionsOfTyr();
        SteadfastPaladin paladin = new SteadfastPaladin();
        harness.setHand(player1, List.of(champions, paladin));
        harness.addMana(player1, ManaColor.WHITE, 6);


        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return paladin;
    }

    @Test
    void boonRemainsAfterChampionsLeavesBattlefield() {
        ChampionsOfTyr champions = new ChampionsOfTyr();
        harness.setHand(player1, List.of(champions));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent source = findPermanent(player1, "Champions of Tyr");
        source.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Champions of Tyr");

        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a flying counter on that creature");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Steadfast Paladin").getCounterCount(CounterType.FLYING))
                .isEqualTo(1);
    }

    @Test
    void doubleTeamConjuresCreatureAndBothLoseDoubleTeam() {
        Permanent champions = addCreatureReady(player1, new ChampionsOfTyr());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, champions, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(ChampionsOfTyr.class);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent conjured = findPermanents(player1, "Champions of Tyr").stream()
                .filter(permanent -> !permanent.getId().equals(champions.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, conjured, Keyword.DOUBLE_TEAM)).isFalse();

        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a +1/+1 counter on that creature");
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Steadfast Paladin").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void multipleBoonsApplyIndependentChoicesToSameCreature() {
        harness.enterBattlefieldAndReturn(player1, new ChampionsOfTyr());
        harness.enterBattlefieldAndReturn(player1, new ChampionsOfTyr());
        resolveAllTriggers();
        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a +1/+1 counter on that creature");
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a flying counter on that creature");
        resolveAllTriggers();

        Permanent paladin = findPermanent(player1, "Steadfast Paladin");
        assertThat(paladin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(paladin.getCounterCount(CounterType.FLYING)).isEqualTo(1);
    }

    @Test
    void opponentsCreatureSpellDoesNotConsumeBoon() {
        harness.enterBattlefieldAndReturn(player1, new ChampionsOfTyr());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SteadfastPaladin()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);
        resolveAllTriggers();
        assertThat(findPermanent(player2, "Steadfast Paladin").getCounterCount(CounterType.FLYING))
                .isZero();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SteadfastPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Put a flying counter on that creature");
        resolveAllTriggers();
        assertThat(findPermanent(player1, "Steadfast Paladin").getCounterCount(CounterType.FLYING))
                .isEqualTo(1);
    }

    private Permanent findPermanent(UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
