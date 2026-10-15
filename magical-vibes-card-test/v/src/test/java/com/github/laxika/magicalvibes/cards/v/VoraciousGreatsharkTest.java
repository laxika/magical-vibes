package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TheOzolith;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoraciousGreatshark.class, GrizzlyBears.class, Ornithopter.class, LightningBolt.class,
        AlmightyBrushwagg.class, TheOzolith.class})
class VoraciousGreatsharkTest extends BaseCardTest {

    @Test
    @DisplayName("ETB counters a target creature spell")
    void etbCountersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        VoraciousGreatshark shark = new VoraciousGreatshark();
        harness.setHand(player1, List.of(bears, shark));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(bears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Voracious Greatshark");
    }

    @Test
    @DisplayName("ETB counters a target artifact creature spell")
    void etbCountersArtifactCreatureSpell() {
        Ornithopter ornithopter = new Ornithopter();
        VoraciousGreatshark shark = new VoraciousGreatshark();
        harness.setHand(player1, List.of(ornithopter, shark));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castArtifact(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ornithopter.getId());

        harness.handlePermanentChosen(player1, ornithopter.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Voracious Greatshark");
    }

    @Test
    @DisplayName("ETB cannot target a noncreature, nonartifact spell")
    void etbCannotTargetNoncreatureNonartifactSpell() {
        LightningBolt bolt = new LightningBolt();
        VoraciousGreatshark shark = new VoraciousGreatshark();
        harness.setHand(player2, List.of(bolt));
        harness.setHand(player1, List.of(shark));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Voracious Greatshark");
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Flash allows countering an opponent's creature spell on their turn")
    void etbCountersOpponentsCreatureSpell() {
        AlmightyBrushwagg brushwagg = new AlmightyBrushwagg();
        harness.setHand(player2, List.of(brushwagg));
        harness.setHand(player1, List.of(new VoraciousGreatshark()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(brushwagg.getId());
        harness.handlePermanentChosen(player1, brushwagg.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertOnBattlefield(player1, "Voracious Greatshark");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB counters a noncreature artifact spell")
    void etbCountersNoncreatureArtifactSpell() {
        TheOzolith ozolith = new TheOzolith();
        harness.setHand(player2, List.of(ozolith));
        harness.setHand(player1, List.of(new VoraciousGreatshark()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.castArtifact(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ozolith.getId());
        harness.handlePermanentChosen(player1, ozolith.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "The Ozolith");
        harness.assertNotOnBattlefield(player2, "The Ozolith");
        harness.assertOnBattlefield(player1, "Voracious Greatshark");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash can be cast in an opponent's end step without a spell to counter")
    void entersWithoutLegalTarget() {
        harness.setHand(player1, List.of(new VoraciousGreatshark()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passPriority(player2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Voracious Greatshark");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot counter a creature's activated ability")
    void etbCannotTargetCreatureActivatedAbility() {
        Permanent brushwagg = harness.addToBattlefieldAndReturn(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new VoraciousGreatshark()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, brushwagg)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, brushwagg)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Voracious Greatshark");
        assertThat(gd.stack).isEmpty();
    }
}
