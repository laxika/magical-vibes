package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightUpTheStage;
import com.github.laxika.magicalvibes.cards.s.SoulfireGrandMaster;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelayedBlastFireball.class, FugitiveWizard.class, GarrukWildspeaker.class,
        GrizzlyBears.class, HillGiant.class, LightUpTheStage.class, SoulfireGrandMaster.class,
        AjanisPridemate.class, Twincast.class})
class DelayedBlastFireballTest extends BaseCardTest {

    @Test
    void handCastDealsTwoDamageToOpponentsAndTheirCreaturesOnly() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPlaneswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        opponentPlaneswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new DelayedBlastFireball()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opponentPlaneswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void foretoldCastDealsFiveDamageToOpponentsAndTheirCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new FugitiveWizard());
        DelayedBlastFireball spell = new DelayedBlastFireball();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(spell.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Delayed Blast Fireball");
    }

    @Test
    void foretoldCastKillsThreeToughnessCreatureAndSparesOwnCreaturesAndPlaneswalkers() {
        harness.addToBattlefield(player2, new HillGiant());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        DelayedBlastFireball spell = new DelayedBlastFireball();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void foretoldCardCannotBeCastOnTheTurnItWasForetold() {
        DelayedBlastFireball spell = new DelayedBlastFireball();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretellCannotBeUsedDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new DelayedBlastFireball()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Delayed Blast Fireball");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void castFromExileWithoutForetellDealsFiveDamageForNormalManaCost() {
        DelayedBlastFireball spell = new DelayedBlastFireball();
        harness.setLibrary(player1, List.of(spell, new FugitiveWizard()));
        harness.setHand(player1, List.of(new LightUpTheStage()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player1, "Delayed Blast Fireball");
    }

    @Test
    void copyOfSpellCastFromExileDealsOnlyTwoDamage() {
        DelayedBlastFireball spell = new DelayedBlastFireball();
        harness.setHand(player1, List.of(spell, new Twincast()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.castFromExile(player1, spell.getId());
        harness.castInstant(player1, 0, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Hill Giant");

        harness.passBothPriorities();

        harness.assertLife(player2, 13);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void lifelinkDamageToPlayerAndCreatureCausesOnlyOneLifeGainTrigger() {
        harness.addToBattlefield(player1, new SoulfireGrandMaster());
        Permanent pridemate = harness.addToBattlefieldAndReturn(player1, new AjanisPridemate());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DelayedBlastFireball()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
