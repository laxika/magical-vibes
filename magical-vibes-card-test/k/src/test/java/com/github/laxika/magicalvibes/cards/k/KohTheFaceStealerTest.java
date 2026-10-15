package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.r.Riftsweeper;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SistersOfStoneDeath;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
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

@CardUsed({KohTheFaceStealer.class, GrizzlyBears.class, ProdigalSorcerer.class,
        Shock.class, SoulWarden.class, DayOfJudgment.class, Riftsweeper.class,
        SistersOfStoneDeath.class})
class KohTheFaceStealerTest extends BaseCardTest {

    @Test
    void entersAndExilesAnotherTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KohTheFaceStealer()));
        addKohMana();

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Grizzly Bears")).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Koh, the Face Stealer").getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    void choosesAnExiledCreatureAndCopiesItsActivatedAbility() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Card bears = new GrizzlyBears();
        Card sorcerer = new ProdigalSorcerer();
        gd.addToExile(player1.getId(), bears, koh.getId());
        gd.addToExile(player1.getId(), sorcerer, koh.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.KohExiledCreatureChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(sorcerer.getId()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(koh.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void copiesTriggeredAbilitiesOfTheChosenCreature() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Card soulWarden = new SoulWarden();
        gd.addToExile(player1.getId(), soulWarden, koh.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void mayExileAnotherNontokenCreatureThatDies() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(koh.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getCard().getId());
    }

    @Test
    void canEnterWithoutExilingACreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.castFromHand(player1, new KohTheFaceStealer(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Koh, the Face Stealer");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Koh, the Face Stealer").getId()))
                .isEmpty();
    }

    @Test
    void canPayLifeWithNoCreatureCardsExiled() {
        addCreatureReady(player1, new KohTheFaceStealer());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.assertLife(player1, 19);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canDeclineExilingADyingCreature() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(koh.getId())).isEmpty();
    }

    @Test
    void choosingAnotherCardReplacesPreviouslyGainedAbilities() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Card sorcerer = new ProdigalSorcerer();
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), sorcerer, koh.getId());
        gd.addToExile(player1.getId(), bears, koh.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcerer.getId()));
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(koh.isTapped()).isFalse();
    }

    @Test
    void losesGainedAbilitiesWhenTheChosenCardLeavesExile() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Card sorcerer = new ProdigalSorcerer();
        gd.addToExile(player1.getId(), sorcerer, koh.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Riftsweeper(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(sorcerer.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(sorcerer.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(sorcerer);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        assertThat(koh.isTapped()).isFalse();
    }

    @Test
    void seesOtherCreaturesDieSimultaneouslyWithIt() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.castFromHand(player1, new DayOfJudgment(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getCardsExiledByPermanent(koh.getId()))
                .extracting(Card::getId).containsExactly(bears.getCard().getId());
        harness.assertInGraveyard(player1, "Koh, the Face Stealer");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void copiedLinkedReturnAbilityCannotReturnCardsExiledByKohsOwnAbilities() {
        Permanent koh = addCreatureReady(player1, new KohTheFaceStealer());
        Card sisters = new SistersOfStoneDeath();
        gd.addToExile(player2.getId(), sisters, koh.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(sisters.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Sisters of Stone Death");
        harness.assertNotOnBattlefield(player2, "Sisters of Stone Death");
    }

    private void addKohMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
