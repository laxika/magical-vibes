package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WildsongHowler;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlpackPiper.class, WildsongHowler.class, HowlpackWolf.class, GrizzlyBears.class, Forest.class})
class HowlpackPiperTest extends BaseCardTest {

    @Test
    @DisplayName("A Wolf put onto the battlefield untaps Howlpack Piper")
    void wolfUntapsPiper() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new HowlpackWolf()));
        addAbilityMana();

        activateAndResolveCardChoice();

        assertThat(piper.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Howlpack Wolf");
    }

    @Test
    @DisplayName("A non-Wolf creature put onto the battlefield does not untap Howlpack Piper")
    void nonWolfDoesNotUntapPiper() {
        Permanent piper = addReadyPiper();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addAbilityMana();

        activateAndResolveCardChoice();

        assertThat(piper.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Wildsong Howler looks at six cards when it enters at night")
    void wildsongHowlerLooksAtTopSixOnEntry() {
        gd.dayNight = DayNight.NIGHT;
        Card wolf = new HowlpackWolf();
        harness.setLibrary(player1, List.of(
                new Forest(), wolf, new Forest(), new Forest(), new Forest(), new Forest()));

        Permanent howler = harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());
        harness.passBothPriorities();

        assertThat(howler.isTransformed()).isTrue();
        assertThat(howler.getCard()).isInstanceOf(WildsongHowler.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactly(wolf.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Transforming into Wildsong Howler triggers its library ability")
    void wildsongHowlerLooksAtTopSixWhenItTransforms() {
        gd.dayNight = DayNight.DAY;
        Card wolf = new HowlpackWolf();
        harness.setLibrary(player1, List.of(
                new Forest(), wolf, new Forest(), new Forest(), new Forest(), new Forest()));
        Permanent howler = harness.enterBattlefieldAndReturn(player1, new HowlpackPiper());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);
        harness.passBothPriorities();

        assertThat(howler.isTransformed()).isTrue();
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(wolf.getId());
    }

    private Permanent addReadyPiper() {
        Permanent piper = new Permanent(new HowlpackPiper());
        piper.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(piper);
        return piper;
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void activateAndResolveCardChoice() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
    }
}
