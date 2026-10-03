package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SanctumOfTheSun;
import com.github.laxika.magicalvibes.cards.t.Twitch;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AzorsGateway.class, SanctumOfTheSun.class, ColossalDreadmaw.class, Forest.class,
        GrizzlyBears.class, HillGiant.class, Shock.class, Twitch.class})
class AzorsGatewayTest extends BaseCardTest {

    @Test
    void drawsThenExilesAChosenCardAndTracksItWithGateway() {
        Permanent gateway = addGateway();
        Card drawn = new Shock();
        Card exiled = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getCardsExiledByPermanent(gateway.getId())).containsExactly(exiled);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gateway.isTransformed()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void transformsAfterFiveDifferentManaValuesAndBackFaceAddsCurrentLifeTotal() {
        Permanent gateway = addGateway();
        gd.addToExile(player1.getId(), new Forest(), gateway.getId());
        gd.addToExile(player1.getId(), new Shock(), gateway.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());

        Card drawn = new Shock();
        Card exiled = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gateway.isTransformed()).isTrue();
        assertThat(gateway.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        assertThat(gd.getCardsExiledByPermanent(gateway.getId())).contains(exiled);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(25);
    }

    @Test
    void doesNotTransformWhenExiledCardsHaveOnlyFourDifferentManaValues() {
        Permanent gateway = addGateway();
        gd.addToExile(player1.getId(), new Forest(), gateway.getId());
        gd.addToExile(player1.getId(), new Shock(), gateway.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());

        Card drawn = new Shock();
        Card exiled = new HillGiant();
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(exiled));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gateway.isTransformed()).isFalse();
        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void canExileTheJustDrawnCardWhenHandWasEmpty() {
        Permanent gateway = addGateway();
        Card drawn = new ColossalDreadmaw();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(gateway.getId())).containsExactly(drawn);
        assertThat(gateway.isTransformed()).isFalse();
    }

    @Test
    void cardsExiledWithAnotherGatewayDoNotCountTowardTransformation() {
        Permanent gateway = addGateway();
        Permanent otherGateway = harness.addToBattlefieldAndReturn(player2, new AzorsGateway());
        gd.addToExile(player2.getId(), new Forest(), otherGateway.getId());
        gd.addToExile(player2.getId(), new Shock(), otherGateway.getId());
        gd.addToExile(player2.getId(), new GrizzlyBears(), otherGateway.getId());
        gd.addToExile(player2.getId(), new HillGiant(), otherGateway.getId());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gateway.isTransformed()).isFalse();
        assertThat(gateway.isTapped()).isTrue();
        harness.assertLife(player1, 20);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void sanctumAddsOneChosenColorUsingLifeTotalAtActivation(ManaColor color) {
        Permanent gateway = addGateway();
        gd.addToExile(player1.getId(), new Forest(), gateway.getId());
        gd.addToExile(player1.getId(), new Shock(), gateway.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gateway.isTransformed()).isTrue();

        harness.setLife(player1, 13);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gateway.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor manaColor : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(manaColor))
                    .isEqualTo(manaColor == color ? 13 : 0);
        }
    }

    @Test
    void olderActivationDoesNotTransformSanctumBackAfterANewerActivationTransformsIt() {
        Permanent gateway = addGateway();
        gd.addToExile(player1.getId(), new Forest(), gateway.getId());
        gd.addToExile(player1.getId(), new Shock(), gateway.getId());
        gd.addToExile(player1.getId(), new GrizzlyBears(), gateway.getId());
        gd.addToExile(player1.getId(), new HillGiant(), gateway.getId());
        gd.addToExile(player1.getId(), new ColossalDreadmaw(), gateway.getId());
        harness.setHand(player1, List.of(new Twitch()));
        harness.setLibrary(player1, List.of(new Forest(), new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.castAndResolveInstant(player1, 0, gateway.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gateway.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gateway.isTransformed()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertLife(player1, 30);
        assertThat(gateway.isTapped()).isFalse();
        assertThat(gateway.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addGateway() {
        return addCreatureReady(player1, new AzorsGateway());
    }
}
