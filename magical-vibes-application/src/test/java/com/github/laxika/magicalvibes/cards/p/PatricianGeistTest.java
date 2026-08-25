package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.y.YawgmothsAgenda;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PatricianGeist.class, GrizzlyBears.class,
        YawgmothsAgenda.class, LightningStrike.class})
class PatricianGeistTest extends BaseCardTest {

    @Test
    void boostsOtherSpiritsYouControl() {
        addCreatureReady(player1, new PatricianGeist());
        addCreatureReady(player1, spirit());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, spirit());

        Permanent geist = findPermanent(player1, "Patrician Geist");
        Permanent ownSpirit = findPermanent(player1, "Test Spirit");
        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingSpirit = findPermanent(player2, "Test Spirit");

        assertThat(gqs.getEffectivePower(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, geist)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, ownSpirit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownSpirit)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingSpirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingSpirit)).isEqualTo(2);
    }

    @Test
    void reducesSpellsCastFromYourGraveyard() {
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.addToBattlefield(player1, new YawgmothsAgenda());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromGraveyardTargeting(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void doesNotReduceSpellsCastFromHand() {
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Card spirit() {
        Card spirit = new Card();
        spirit.setName("Test Spirit");
        spirit.setType(CardType.CREATURE);
        spirit.setSubtypes(List.of(CardSubtype.SPIRIT));
        spirit.setPower(2);
        spirit.setToughness(2);
        return spirit;
    }
}
