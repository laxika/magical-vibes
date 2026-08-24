package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AngrathsRampage.class, ChandraNalaar.class, FountainOfYouth.class, GrizzlyBears.class, Millstone.class})
class AngrathsRampageTest extends BaseCardTest {

    @Test
    @DisplayName("Artifact mode makes the target player sacrifice an artifact of their choice")
    void artifactModeSacrificesChosenArtifact() {
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        harness.addToBattlefield(player2, new FountainOfYouth());

        cast(0);
        harness.handleMultiplePermanentsChosen(player2, List.of(millstone.getId()));

        harness.assertInGraveyard(player2, "Millstone");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Creature mode makes the target player sacrifice a creature of their choice")
    void creatureModeSacrificesChosenCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(1);
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(1);
    }

    @Test
    @DisplayName("Planeswalker mode makes the target player sacrifice a planeswalker")
    void planeswalkerModeSacrificesPlaneswalker() {
        Permanent chandra = addPlaneswalker(player2, new ChandraNalaar(), 5);

        cast(2);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chandra);
    }

    @Test
    @DisplayName("The modes can target any player")
    void modesCanTargetController() {
        harness.addToBattlefield(player1, new FountainOfYouth());

        harness.setHand(player1, List.of(new AngrathsRampage()));
        addMana();
        harness.castModalSorcery(player1, 0, 0, List.of(player1.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    @DisplayName("A mode requires a player target")
    void modeCannotTargetPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        harness.setHand(player1, List.of(new AngrathsRampage()));
        addMana();

        UUID artifactId = artifact.getId();
        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, 0, List.of(artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");
    }

    private void cast(int modeIndex) {
        harness.setHand(player1, List.of(new AngrathsRampage()));
        addMana();
        harness.castModalSorcery(player1, 0, modeIndex, List.of(player2.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player,
                                      Card planeswalkerCard, int loyalty) {
        Permanent planeswalker = new Permanent(planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
