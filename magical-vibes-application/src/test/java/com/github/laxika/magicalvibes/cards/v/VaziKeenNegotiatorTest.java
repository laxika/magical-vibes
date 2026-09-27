package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VaziKeenNegotiator.class, WilyGoblin.class, GiantGrowth.class,
        GrizzlyBears.class, ForswornPaladin.class})
class VaziKeenNegotiatorTest extends BaseCardTest {

    @Test
    void createsAsManyTreasuresAsTheControllerCreatedThisTurn() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(gd.getTreasureTokensCreatedThisTurn(player1.getId())).isEqualTo(1);
        assertThat(vazi.isTapped()).isTrue();
    }

    @Test
    void treasurePaidOpponentSpellPutsCounterOnCreatureAndDraws() {
        addCreatureReady(player1, new VaziKeenNegotiator());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent treasure = addTreasureToken(player2);
        Card drawn = new Card();
        drawn.setName("Drawn card");
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");
        harness.castInstant(player2, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(treasure);
    }

    @Test
    void treasurePaidOpponentAbilityAlsoTriggers() {
        addCreatureReady(player1, new VaziKeenNegotiator());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ForswornPaladin());
        Permanent treasure = addTreasureToken(player2);
        Card drawn = new Card();
        drawn.setName("Drawn card");
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        int treasureIndex = gd.playerBattlefields.get(player2.getId()).indexOf(treasure);
        harness.activateAbility(player2, treasureIndex, null, null);
        harness.handleListChoice(player2, "BLACK");
        harness.activateAbility(player2, 0, 1, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private Permanent addTreasureToken(com.github.laxika.magicalvibes.model.Player player) {
        Card treasureCard = new Card();
        treasureCard.setName("Treasure");
        treasureCard.setType(CardType.ARTIFACT);
        treasureCard.setToken(true);
        treasureCard.setSubtypes(List.of(CardSubtype.TREASURE));
        treasureCard.addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeSelfCost(), new AwardAnyColorManaEffect()),
                "{T}, Sacrifice this artifact: Add one mana of any color."));
        Permanent treasure = new Permanent(treasureCard);
        treasure.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(treasure);
        return treasure;
    }
}
