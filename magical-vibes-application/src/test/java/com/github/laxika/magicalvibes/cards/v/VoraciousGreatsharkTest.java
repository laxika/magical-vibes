package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VoraciousGreatshark.class, GrizzlyBears.class, Ornithopter.class, LightningBolt.class})
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
}
