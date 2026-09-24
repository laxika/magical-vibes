package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.e.EmpressGalina;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MoxAmber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JodahTheUnifier.class, CaptainSisay.class, EmpressGalina.class,
        Forest.class, GrizzlyBears.class, MoxAmber.class})
class JodahTheUnifierTest extends BaseCardTest {

    @Test
    void legendaryCreaturesGetBonusIncludingJodahItself() {
        Permanent sisay = addCreatureReady(player1, new CaptainSisay());
        Permanent galina = addCreatureReady(player1, new EmpressGalina());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int sisayPower = gqs.getEffectivePower(gd, sisay);
        int sisayToughness = gqs.getEffectiveToughness(gd, sisay);
        int galinaPower = gqs.getEffectivePower(gd, galina);
        int bearsPower = gqs.getEffectivePower(gd, bears);

        Permanent jodah = addCreatureReady(player1, new JodahTheUnifier());

        assertThat(gqs.getEffectivePower(gd, sisay)).isEqualTo(sisayPower + 3);
        assertThat(gqs.getEffectiveToughness(gd, sisay)).isEqualTo(sisayToughness + 3);
        assertThat(gqs.getEffectivePower(gd, galina)).isEqualTo(galinaPower + 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(bearsPower);
        assertThat(gqs.getEffectivePower(gd, jodah)).isEqualTo(jodah.getCard().getPower() + 3);
    }

    @Test
    void legendarySpellExilesAndOffersFirstLowerManaValueLegendaryNonland() {
        setupJodah();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        CaptainSisay equalManaValueLegend = new CaptainSisay();
        MoxAmber mox = new MoxAmber();
        harness.setLibrary(player1, List.of(forest, bears, equalManaValueLegend, mox));

        castCaptainSisay();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == mox);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(forest, bears, equalManaValueLegend);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == mox
                && entry.getEntryType() == StackEntryType.ARTIFACT_SPELL);
    }

    @Test
    void decliningFreeCastLeavesTheHitExiled() {
        setupJodah();
        Forest forest = new Forest();
        MoxAmber mox = new MoxAmber();
        harness.setLibrary(player1, List.of(forest, mox));

        castCaptainSisay();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == mox);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(mox);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void nonlegendarySpellDoesNotTrigger() {
        setupJodah();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    private void setupJodah() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new JodahTheUnifier());
    }

    private void castCaptainSisay() {
        harness.setHand(player1, List.of(new CaptainSisay()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }
}
