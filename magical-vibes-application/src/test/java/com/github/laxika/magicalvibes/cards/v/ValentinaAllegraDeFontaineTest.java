package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaSuperSoldier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MODOK;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValentinaAllegraDeFontaine.class, MODOK.class, GrizzlyBears.class,
        CaptainAmericaSuperSoldier.class})
class ValentinaAllegraDeFontaineTest extends BaseCardTest {

    @Test
    void makesOtherVillainsYouControlHeroes() {
        Permanent valentina = addCreatureReady(player1, new ValentinaAllegraDeFontaine());
        Permanent ownVillain = addCreatureReady(player1, new MODOK());
        Permanent ownNonVillain = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVillain = addCreatureReady(player2, new MODOK());

        assertThat(gqs.hasEffectiveSubtype(gd, ownVillain, CardSubtype.VILLAIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ownVillain, CardSubtype.HERO)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ownNonVillain, CardSubtype.HERO)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, opponentVillain, CardSubtype.HERO)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, valentina, CardSubtype.HERO)).isFalse();
    }

    @Test
    void tapsToMakeAControlledHeroConnive() {
        addCreatureReady(player1, new ValentinaAllegraDeFontaine());
        Permanent target = addCreatureReady(player1, new MODOK());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void conniveRejectsNonHeroAndOpponentsHero() {
        addCreatureReady(player1, new ValentinaAllegraDeFontaine());
        Permanent nonHero = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentHero = addCreatureReady(player2, new CaptainAmericaSuperSoldier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonHero.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Hero you control");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentHero.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Hero you control");
    }

    @Test
    void conniveAbilityIsSorcerySpeedOnly() {
        addCreatureReady(player1, new ValentinaAllegraDeFontaine());
        Permanent target = addCreatureReady(player1, new MODOK());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
