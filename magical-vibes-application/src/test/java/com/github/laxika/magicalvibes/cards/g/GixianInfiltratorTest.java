package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.t.Thraxodemon;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GixianInfiltrator.class, EvolvingWilds.class, Thraxodemon.class})
class GixianInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another permanent puts a +1/+1 counter on Gixian Infiltrator")
    void sacrificingAnotherPermanentPutsCounterOnIt() {
        Permanent infiltrator = addCreatureReady(player1, new GixianInfiltrator());
        addTreasureToken(player1);

        sacrificeTreasure(player1);

        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Treasure");
    }

    @Test
    @DisplayName("Each sacrificed permanent puts a separate counter on Gixian Infiltrator")
    void multipleSacrificesPutMultipleCountersOnIt() {
        Permanent infiltrator = addCreatureReady(player1, new GixianInfiltrator());
        addTreasureToken(player1);
        addTreasureToken(player1);

        sacrificeTreasure(player1);
        sacrificeTreasure(player1);

        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void sacrificeTreasure(Player player) {
        int treasureIndex = findPermanentIndex(player, "Treasure");
        harness.activateAbility(player, treasureIndex, null, null);
        harness.handleListChoice(player, "RED");
        resolveAllTriggers();
    }

    private int findPermanentIndex(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, name));
    }

    @Test
    @DisplayName("Sacrificing an opponent's Treasure does not trigger Gixian Infiltrator")
    void opponentSacrificeDoesNotPutCounterOnIt() {
        Permanent infiltrator = addCreatureReady(player1, new GixianInfiltrator());
        addTreasureToken(player2);

        sacrificeTreasure(player2);

        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Sacrificing a land triggers Gixian Infiltrator")
    void sacrificingLandPutsCounterOnIt() {
        Permanent infiltrator = addCreatureReady(player1, new GixianInfiltrator());
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(infiltrator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Evolving Wilds");
    }

    @Test
    @DisplayName("Sacrificing Gixian Infiltrator itself does not trigger its ability")
    void sacrificingItselfDoesNotTrigger() {
        addCreatureReady(player1, new Thraxodemon());
        harness.addToBattlefield(player1, new GixianInfiltrator());
        harness.setLibrary(player1, List.of(new GixianInfiltrator()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Gixian Infiltrator");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() instanceof GixianInfiltrator);
        assertThat(gd.pendingManaAbilityTriggers)
                .noneMatch(entry -> entry.getCard() instanceof GixianInfiltrator);
        resolveAllTriggers();
    }

    private void addTreasureToken(Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."
        ));

        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
    }
}
