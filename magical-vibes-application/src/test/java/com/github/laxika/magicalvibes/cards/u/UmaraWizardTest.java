package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaraWizard.class, UmaraSkyfalls.class, Divination.class, FugitiveWizard.class,
        GrizzlyBears.class, Shock.class})
class UmaraWizardTest extends BaseCardTest {

    @Test
    void gainsFlyingWhenInstantIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
    }

    @Test
    void gainsFlyingWhenSorceryIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
    }

    @Test
    void gainsFlyingWhenWizardIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotGainFlyingWhenUnrelatedCreatureIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingWearsOffAtEndOfTurn() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent afterCleanup = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof UmaraWizard)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.hasKeyword(gd, afterCleanup, Keyword.FLYING)).isFalse();
    }

    @Test
    void landFaceEntersTappedAndProducesBlueMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UmaraWizard()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(UmaraSkyfalls.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
    }
}
