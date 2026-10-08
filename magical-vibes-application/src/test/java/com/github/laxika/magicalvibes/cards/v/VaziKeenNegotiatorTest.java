package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.cards.f.ForsakeTheWorldly;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.cards.w.WitchbaneOrb;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaziKeenNegotiator.class, WilyGoblin.class, GiantGrowth.class,
        GrizzlyBears.class, ForswornPaladin.class, WitchbaneOrb.class, ForsakeTheWorldly.class})
class VaziKeenNegotiatorTest extends BaseCardTest {

    @Test
    void createsAsManyTreasuresAsTheControllerCreatedThisTurn() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.activateAbility(player1, 0, null, player2.getId());
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
        harness.setHand(player1, List.of());
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
        harness.setHand(player1, List.of());
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

    @Test
    void createsNoTreasuresWhenNoneWereCreatedThisTurn() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(vazi.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithoutAnOpponentTarget() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(vazi.isTapped()).isFalse();
    }

    @Test
    void cannotTargetOpponentWithHexproof() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        harness.addToBattlefield(player2, new WitchbaneOrb());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(vazi.isTapped()).isFalse();
    }

    @Test
    void opponentSpellPaidWithOrdinaryManaDoesNotTrigger() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, vazi.getId());
        resolveAllTriggers();

        assertThat(vazi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void controllersOwnTreasurePaidSpellDoesNotTrigger() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        addTreasureToken(player1);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GiantGrowth()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castInstant(player1, 0, vazi.getId());
        resolveAllTriggers();

        assertThat(vazi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotDrawWhenTheTriggeredAbilitysOnlyTargetLeaves() {
        addCreatureReady(player1, new VaziKeenNegotiator());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addTreasureToken(player2);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");
        harness.castInstant(player2, 0, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void countsTreasuresCreatedEvenAfterTheyAreSacrificed() {
        addCreatureReady(player1, new VaziKeenNegotiator());
        harness.setHand(player1, List.of(new WilyGoblin(), new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.activateAbility(player1, 2, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
    }

    @Test
    void opponentAbilityPaidWithOrdinaryManaDoesNotTrigger() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        addCreatureReady(player2, new ForswornPaladin());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, 1, null, vazi.getId());
        resolveAllTriggers();

        assertThat(vazi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void treasurePaidOpponentCyclingTriggersCounterAndDraw() {
        Permanent vazi = addCreatureReady(player1, new VaziKeenNegotiator());
        addTreasureToken(player2);
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawn));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new ForsakeTheWorldly()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "GREEN");
        harness.activateHandAbility(player2, 0, null);
        harness.handlePermanentChosen(player1, vazi.getId());
        resolveAllTriggers();

        assertThat(vazi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
        return addCreatureReady(player, treasureCard);
    }
}
