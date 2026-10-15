package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.e.ExpeditionDiviner;
import com.github.laxika.magicalvibes.cards.f.FieldResearch;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UmaraWizard.class, UmaraSkyfalls.class, FieldResearch.class, ExpeditionDiviner.class,
        CanopyBaloth.class, IntoTheRoil.class})
class UmaraWizardTest extends BaseCardTest {

    @Test
    void gainsFlyingWhenInstantIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        Permanent target = addCreatureReady(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
        harness.assertOnBattlefield(player2, "Canopy Baloth");
    }

    @Test
    void gainsFlyingWhenSorceryIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setLibrary(player1, List.of(new CanopyBaloth(), new CanopyBaloth()));
        harness.castFromHand(player1, new FieldResearch(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
    }

    @Test
    void gainsFlyingWhenWizardIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.castFromHand(player1, new ExpeditionDiviner(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
    }

    @Test
    void doesNotGainFlyingWhenUnrelatedCreatureIsCast() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.castFromHand(player1, new CanopyBaloth(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingWearsOffAtEndOfTurn() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setLibrary(player1, List.of(new CanopyBaloth(), new CanopyBaloth()));
        harness.castFromHand(player1, new FieldResearch(), "{2}{U}");
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isTrue();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
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
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentInstantDoesNotGrantFlying() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        Permanent target = addCreatureReady(player1, new CanopyBaloth());
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
        harness.assertInHand(player1, "Canopy Baloth");
    }

    @Test
    void wizardEnteringWithoutBeingCastDoesNotGrantFlying() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());

        harness.enterBattlefieldAndReturn(player1, new ExpeditionDiviner());

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureFaceDoesNotTriggerForItsOwnCast() {
        harness.castFromHand(player1, new UmaraWizard(), "{4}{U}");
        harness.passBothPriorities();

        Permanent umara = findPermanent(player1, "Umara Wizard");
        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
        assertThat(umara.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void playingLandFaceDoesNotTriggerAnotherUmaraWizard() {
        Permanent umara = addCreatureReady(player1, new UmaraWizard());
        harness.setHand(player1, List.of(new UmaraWizard()));

        gs.playCard(gd, player1, 0, 1, null, null);

        assertThat(gqs.hasKeyword(gd, umara, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Umara Skyfalls");
    }
}
