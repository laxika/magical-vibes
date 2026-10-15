package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OverwhelmingRemorse.class, GrizzlyBears.class, GarrukWildspeaker.class, Plains.class,
        ArgothianSprite.class})
class OverwhelmingRemorseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature")
    void exilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiles a target planeswalker")
    void exilesTargetPlaneswalker() {
        Permanent target = addReadyPlaneswalker(player2, 3);
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Garruk Wildspeaker"));
    }

    @Test
    @DisplayName("Costs one less for each creature card in its controller's graveyard")
    void reducesCostForCreatureCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a land")
    void rejectsLandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker");
    }

    @Test
    @DisplayName("Excess creature cards reduce the cost to one black mana")
    void excessCreatureCardsReduceOnlyGenericCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite(),
                new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Argothian Sprite"));
    }

    @Test
    @DisplayName("The black mana requirement remains even with excess creature cards")
    void reductionDoesNotRemoveBlackManaRequirement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setGraveyard(player1, List.of(new ArgothianSprite(), new ArgothianSprite(),
                new ArgothianSprite(), new ArgothianSprite(), new ArgothianSprite()));
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Overwhelming Remorse");
        harness.assertOnBattlefield(player2, "Argothian Sprite");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature cards in an opponent's graveyard do not reduce the cost")
    void ignoresOpponentGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setGraveyard(player2, List.of(new ArgothianSprite()));
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Overwhelming Remorse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Noncreature cards in the controller's graveyard do not reduce the cost")
    void ignoresNoncreatureCards() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setGraveyard(player1, List.of(new Plains(), new OverwhelmingRemorse()));
        harness.setHand(player1, List.of(new OverwhelmingRemorse()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Overwhelming Remorse");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
