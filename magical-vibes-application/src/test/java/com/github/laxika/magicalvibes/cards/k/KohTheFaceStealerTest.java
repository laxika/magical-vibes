package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({KohTheFaceStealer.class, GrizzlyBears.class, ProdigalSorcerer.class,
        Shock.class, SoulWarden.class})
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

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
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

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(koh.getId()))
                .extracting(Card::getId)
                .containsExactly(target.getCard().getId());
    }

    private void addKohMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
