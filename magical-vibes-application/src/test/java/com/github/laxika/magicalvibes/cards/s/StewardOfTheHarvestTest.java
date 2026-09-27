package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StewardOfTheHarvest.class, Forest.class, GrizzlyBears.class, RodOfRuin.class})
class StewardOfTheHarvestTest extends BaseCardTest {

    @Test
    void exilesUpToThreeLandCardsOnlyFromControllersGraveyard() {
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        Card nonland = new RodOfRuin();
        Card opponentLand = new Forest();
        harness.setGraveyard(player1, List.of(first, second, third, fourth, nonland));
        harness.setGraveyard(player2, List.of(opponentLand));

        Permanent steward = castSteward();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.cards()).containsExactly(first, second, third, fourth);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(steward.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fourth, nonland);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void creaturesYouControlGainExiledLandAbilitiesIncludingBasicLandMana() {
        Permanent steward = addReady(new StewardOfTheHarvest());
        Permanent bears = addReady(new GrizzlyBears());
        Card forest = new Forest();
        gd.addToExile(player1.getId(), forest, steward.getId());

        assertThat(gqs.computeStaticBonus(gd, steward).grantedActivatedAbilities()).hasSize(1);
        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).hasSize(1);

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void noncreaturesAndOpponentsCreaturesDoNotGainTheAbilities() {
        Permanent steward = addReady(new StewardOfTheHarvest());
        Permanent bears = addReady(new GrizzlyBears());
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.addToExile(player1.getId(), new Forest(), steward.getId());

        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).hasSize(1);
        assertThat(gqs.computeStaticBonus(gd, rod).grantedActivatedAbilities()).isEmpty();
        assertThat(gqs.computeStaticBonus(gd, opponentBears).grantedActivatedAbilities()).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castSteward() {
        harness.setHand(player1, List.of(new StewardOfTheHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
