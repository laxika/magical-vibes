package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.m.MemoryDeluge;
import com.github.laxika.magicalvibes.cards.o.OtherworldlyGaze;
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
        YawgmothsAgenda.class, LightningStrike.class, OtherworldlyGaze.class, MemoryDeluge.class})
class PatricianGeistTest extends BaseCardTest {

    @Test
    void boostsOtherSpiritsYouControl() {
        Permanent geist = addCreatureReady(player1, new PatricianGeist());
        Permanent ownSpirit = addCreatureReady(player1, spirit());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingSpirit = addCreatureReady(player2, spirit());

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

    @Test
    void reducesFlashbackCost() {
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.setGraveyard(player1, List.of(new OtherworldlyGaze()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Otherworldly Gaze");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleGeistsCannotReduceColoredFlashbackCost() {
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.setGraveyard(player1, List.of(new OtherworldlyGaze()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Otherworldly Gaze");
    }

    @Test
    void doesNotReduceOpponentsFlashbackCost() {
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.setGraveyard(player2, List.of(new OtherworldlyGaze()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFlashback(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Otherworldly Gaze");
    }

    @Test
    void multipleGeistsBoostEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PatricianGeist());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PatricianGeist());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    void multipleGeistsStackTheirGraveyardCostReductions() {
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.addToBattlefield(player1, new PatricianGeist());
        harness.setGraveyard(player1, List.of(new MemoryDeluge()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Memory Deluge");
        assertThat(gd.stack).isEmpty();
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
