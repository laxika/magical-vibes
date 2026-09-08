package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RescueSkiff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScroungeForEternity.class, GrizzlyBears.class, LlanowarElves.class,
        EntropicBattlecruiser.class, RescueSkiff.class})
class ScroungeForEternityTest extends BaseCardTest {

    @Test
    void sacrificesArtifactReturnsCreatureAndCreatesLander() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        cast(creature, artifact);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(artifact.getCard().getId());
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void returnsSpacecraftWithManaValueFiveOrLess() {
        Card spacecraft = new EntropicBattlecruiser();
        harness.setGraveyard(player1, List.of(spacecraft));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        cast(spacecraft, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(spacecraft.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getCard().getId());
        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    void cannotTargetSpacecraftWithManaValueGreaterThanFive() {
        Card spacecraft = new RescueSkiff();
        harness.setGraveyard(player1, List.of(spacecraft));
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, spacecraft.getId(), creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(creature.getCard().getId());
    }

    private void cast(Card target, Permanent sacrifice) {
        prepareCast();
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new ScroungeForEternity()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
