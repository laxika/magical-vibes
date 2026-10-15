package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuthorityOfTheConsuls;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.i.IchorWellspring;
import com.github.laxika.magicalvibes.cards.i.InallaArchmageRitualist;
import com.github.laxika.magicalvibes.cards.n.NikoAris;
import com.github.laxika.magicalvibes.cards.r.RecklessFireweaver;
import com.github.laxika.magicalvibes.cards.t.TatyovaBenthicDruid;
import com.github.laxika.magicalvibes.cards.w.WoodlandChampion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Panharmonicon.class, AuthorityOfTheConsuls.class, ElvishVisionary.class,
        Forest.class, FugitiveWizard.class, IchorWellspring.class, TatyovaBenthicDruid.class,
        PropheticPrism.class, DukharaPeafowl.class, NikoAris.class, WoodlandChampion.class,
        RecklessFireweaver.class, InallaArchmageRitualist.class})
class PanharmoniconTest extends BaseCardTest {

    @Test
    @DisplayName("Panharmonicon doubles a creature's enter-the-battlefield ability")
    void doublesCreatureEnterAbility() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new ElvishVisionary()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Panharmonicon doubles an artifact's enter-the-battlefield ability")
    void doublesArtifactEnterAbility() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new IchorWellspring()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Panharmonicon doubles a controlled trigger caused by an opponent's creature entering")
    void doublesTriggerFromOpponentCreatureEntering() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FugitiveWizard()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Panharmonicon does not double a trigger caused by a land entering")
    void doesNotDoubleLandfallTrigger() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addToBattlefield(player1, new TatyovaBenthicDruid());
        harness.setHand(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Two Panharmonicons add two triggers rather than doubling twice")
    void multipleCopiesAddTriggers() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Panharmonicon does not add triggers to an opponent's permanent")
    void doesNotIncreaseOpponentsTriggers() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new PropheticPrism()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An artifact creature causes only one additional trigger per Panharmonicon")
    void artifactCreatureDoesNotCountTwice() {
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.addToBattlefield(player1, new AuthorityOfTheConsuls());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DukharaPeafowl()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Enchantment-only tokens do not cause additional triggers")
    void doesNotIncreaseTriggersFromShardTokens() {
        harness.addToBattlefield(player1, new Panharmonicon());
        var champion = harness.addToBattlefieldAndReturn(player1, new WoodlandChampion());
        var niko = harness.addToBattlefieldAndReturn(player1, new NikoAris());
        niko.setCounterCount(CounterType.LOYALTY, 3);

        harness.activateAbility(player1, 2, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Panharmonicon applies to triggers caused by its own entry")
    void addsTriggerWhenPanharmoniconItselfEnters() {
        harness.addToBattlefield(player1, new RecklessFireweaver());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Panharmonicon()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Panharmonicon does not increase entry triggers from the command zone")
    void doesNotIncreaseCommandZoneTriggers() {
        harness.addToBattlefield(player1, new Panharmonicon());
        gd.playerCommandZones.get(player1.getId()).add(new InallaArchmageRitualist());
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }
}
