package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScribeOfTheMindful.class, LightningBolt.class, Divination.class, GrizzlyBears.class, Cancel.class})
class ScribeOfTheMindfulTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target instant from your graveyard to hand and sacrifices itself")
    void returnsInstantAndSacrificesSelf() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card bolt = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bolt)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, scribeIndex(scribe), 0, List.of(bolt.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(bolt.getId()));
        // Scribe paid the sacrifice cost and is now in the graveyard; the instant is not.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scribe of the Mindful"))
                .noneMatch(c -> c.getId().equals(bolt.getId()));
        harness.assertNotOnBattlefield(player1, "Scribe of the Mindful");
    }

    @Test
    @DisplayName("Returns a target sorcery from your graveyard to hand")
    void returnsSorcery() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card divination = new Divination();
        harness.setGraveyard(player1, new ArrayList<>(List.of(divination)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, scribeIndex(scribe), 0, List.of(divination.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(divination.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(divination.getId()));
    }

    @Test
    @DisplayName("Cannot target a non-instant/sorcery card (creature) in the graveyard")
    void cannotTargetCreatureCard() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        // Illegal activation rewinds: nothing sacrificed, card stays in the graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getId().equals(bears.getId()));
        harness.assertOnBattlefield(player1, "Scribe of the Mindful");
    }

    @Test
    @DisplayName("Cannot target an instant in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card bolt = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bolt)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(bolt.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificeIsPaidBeforeResolution() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card target = new Cancel();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, scribeIndex(scribe), 0, List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Scribe of the Mindful");
        harness.assertInGraveyard(player1, "Scribe of the Mindful");
        harness.assertInGraveyard(player1, "Cancel");
        harness.assertNotInHand(player1, "Cancel");

        harness.passBothPriorities();
        harness.assertInHand(player1, "Cancel");
        harness.assertNotInGraveyard(player1, "Cancel");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        scribe.setSummoningSick(true);
        Card target = new Cancel();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Scribe of the Mindful");
        assertThat(scribe.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        scribe.setTapped(true);
        Card target = new Cancel();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Scribe of the Mindful");
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    void cannotActivateWithoutMana() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card target = new Cancel();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, scribeIndex(scribe), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Scribe of the Mindful");
        assertThat(scribe.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Cancel");
    }

    @Test
    void doesNotReturnAnotherCardWhenTargetLeavesGraveyard() {
        Permanent scribe = addCreatureReady(player1, new ScribeOfTheMindful());
        Card target = new Cancel();
        Card other = new Cancel();
        harness.setGraveyard(player1, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbilityWithGraveyardTargets(player1, scribeIndex(scribe), 0, List.of(target.getId()));

        gd.playerGraveyards.get(player1.getId()).removeIf(c -> c.getId().equals(target.getId()));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Cancel");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        harness.assertInGraveyard(player1, "Scribe of the Mindful");
    }

    private int scribeIndex(Permanent scribe) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(scribe);
    }
}
