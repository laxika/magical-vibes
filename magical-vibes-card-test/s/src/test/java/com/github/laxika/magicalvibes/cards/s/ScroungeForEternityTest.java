package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
        EntropicBattlecruiser.class, RescueSkiff.class, Forest.class})
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

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card target = new EntropicBattlecruiser();
        harness.setGraveyard(player2, List.of(target));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        prepareCast();
        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
    }

    @Test
    void cannotTargetAnOrdinaryLandCard() {
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        prepareCast();
        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId())).isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
    }

    @Test
    void missingTargetPreventsLanderCreationButDoesNotRefundSacrifice() {
        Card target = new EntropicBattlecruiser();
        harness.setGraveyard(player1, List.of(target));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        prepareCast();
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target, sacrifice.getCard());
        harness.setGraveyard(player1, List.of(sacrifice.getCard()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        harness.assertInGraveyard(player1, "Scrounge for Eternity");
    }

    @Test
    void landerCanFindBasicLandTappedImmediately() {
        Card target = new EntropicBattlecruiser();
        harness.setGraveyard(player1, List.of(target));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new EntropicBattlecruiser());
        cast(target, sacrifice);
        assertThat(findPermanent(player1, "Entropic Battlecruiser").isTapped()).isFalse();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(new ScroungeForEternity(), forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Lander")), null, null);
        assertThat(findPermanents(player1, "Lander")).isEmpty();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        assertThat(findPermanent(player1, "Forest").getCard()).isSameAs(forest);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card instanceof ScroungeForEternity);
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
