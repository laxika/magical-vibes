package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GlaringSpotlight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KayaBaneOfTheDead.class, GrizzlyBears.class, GlaringSpotlight.class,
        IntoTheRoil.class, LeylineOfSanctity.class, Shock.class})
class KayaBaneOfTheDeadTest extends BaseCardTest {

    @Test
    @DisplayName("-3 exiles target creature and removes three loyalty")
    void minusThreeExilesTargetCreature() {
        Permanent kaya = addReadyKaya(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");
        assertThat(kaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("The static ability allows targeting an opponent's hexproof creature")
    void targetsOpponentHexproofCreature() {
        addReadyKaya(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TestCards.mutableCard(creature).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The static ability allows targeting an opponent with hexproof")
    void targetsOpponentWithHexproof() {
        addReadyKaya(5);
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The static ability allows targeting an opponent's hexproof noncreature permanent")
    void targetsOpponentHexproofNoncreaturePermanent() {
        addReadyKaya(5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new GlaringSpotlight());
        TestCards.mutableCard(artifact).setKeywords(EnumSet.of(Keyword.HEXPROOF));
        harness.setHand(player1, List.of(new IntoTheRoil()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Glaring Spotlight");
    }

    @Test
    @DisplayName("The static ability does not bypass shroud")
    void doesNotBypassShroud() {
        addReadyKaya(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TestCards.mutableCard(creature).setKeywords(EnumSet.of(Keyword.SHROUD));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    private Permanent addReadyKaya(int loyalty) {
        Permanent kaya = new Permanent(new KayaBaneOfTheDead());
        kaya.setCounterCount(CounterType.LOYALTY, loyalty);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaya);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }
}
