package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoseFocus;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.OrnithopterOfParadise;
import com.github.laxika.magicalvibes.cards.r.RiptideLaboratory;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoulWatcher.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class,
        LoseFocus.class, OrnithopterOfParadise.class, RiptideLaboratory.class})
class FoulWatcherTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, surveils one")
    void entersWithSurveilOne() {
        GameData gd = harness.getGameData();
        Card topCard = new GrizzlyBears();
        Card remainingCard = new Millstone();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.setHand(player1, List.of(new FoulWatcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.MayAbilityChoice surveil =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(surveil).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    @Test
    @DisplayName("Gets +1/+0 with delirium")
    void getsDeliriumBonus() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addToBattlefield(player1, new FoulWatcher());

        Permanent watcher = findPermanent(player1, "Foul Watcher");
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void mayKeepSurveilledCard() {
        Card topCard = new FoulWatcher();
        Card remainingCard = new LoseFocus();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new FoulWatcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveilling an empty library finishes without a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new FoulWatcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Foul Watcher");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Delirium counts distinct types and updates as the graveyard changes")
    void deliriumUpdatesWithDistinctCardTypes() {
        harness.addToBattlefield(player1, new FoulWatcher());
        Permanent watcher = findPermanent(player1, "Foul Watcher");
        harness.setGraveyard(player1, List.of(
                new FoulWatcher(), new FoulWatcher(), new RiptideLaboratory(), new LoseFocus()));

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(
                new OrnithopterOfParadise(), new RiptideLaboratory(), new LoseFocus()));

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new OrnithopterOfParadise(), new LoseFocus()));

        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable delirium")
    void ignoresOpponentGraveyard() {
        harness.setGraveyard(player1, List.of(new FoulWatcher()));
        harness.setGraveyard(player2, List.of(
                new OrnithopterOfParadise(), new RiptideLaboratory(), new LoseFocus()));
        harness.addToBattlefield(player1, new FoulWatcher());

        Permanent watcher = findPermanent(player1, "Foul Watcher");
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }

    @Test
    @DisplayName("The enter trigger can put enough card types into the graveyard for delirium")
    void surveilEnablesDelirium() {
        Card topCard = new OrnithopterOfParadise();
        Card remainingCard = new FoulWatcher();
        harness.setLibrary(player1, List.of(topCard, remainingCard));
        harness.setGraveyard(player1, List.of(new RiptideLaboratory(), new LoseFocus()));
        harness.setHand(player1, List.of(new FoulWatcher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        Permanent watcher = findPermanent(player1, "Foul Watcher");
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gqs.getEffectivePower(gd, watcher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, watcher)).isEqualTo(2);
    }
}
