package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CybermanPatrol;
import com.github.laxika.magicalvibes.cards.n.NyssaOfTraken;
import com.github.laxika.magicalvibes.cards.g.GoldForgedSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheCyberController.class, Forest.class, GoldForgedSentinel.class, GrizzlyBears.class,
        CybermanPatrol.class, NyssaOfTraken.class})
class TheCyberControllerTest extends BaseCardTest {

    @Test
    void millsEachOpponentAndReturnsAllMilledCreaturesAsBoostedCybermen() {
        Card ownLibraryCard = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        Card opponentLand = new Forest();
        harness.setLibrary(player1, List.of(ownLibraryCard));
        harness.setLibrary(player2, List.of(opponentCreature, opponentLand));
        Permanent existingArtifact = harness.addToBattlefieldAndReturn(player1, new GoldForgedSentinel());
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new TheCyberController()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castArtifact(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent cyberman = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(opponentCreature.getId()))
                .findFirst().orElseThrow();
        assertThat(cyberman.isFaceDown()).isTrue();
        assertThat(cyberman.getFaceDownPower()).isEqualTo(2);
        assertThat(cyberman.getFaceDownToughness()).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, cyberman))
                .containsExactlyInAnyOrder(CardType.ARTIFACT, CardType.CREATURE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, cyberman))
                .containsExactly(CardSubtype.CYBERMAN);
        assertThat(gqs.getEffectivePower(gd, cyberman)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cyberman)).isEqualTo(3);

        assertThat(gqs.getEffectivePower(gd, existingArtifact)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, existingArtifact)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, existingCreature)).isEqualTo(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentLand);
    }

    @Test
    void zeroXDoesNotMillOrReturnCreatures() {
        Card opponentCreature = new GrizzlyBears();
        harness.setLibrary(player2, List.of(opponentCreature));

        harness.setHand(player1, List.of(new TheCyberController()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castArtifact(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(opponentCreature.getId()));
    }

    @Test
    void returnsEveryCreatureFromAShortLibraryWithoutItsPrintedAbilities() {
        Card milledController = new TheCyberController();
        Card milledNyssa = new NyssaOfTraken();
        Card oldGraveyardCreature = new CybermanPatrol();
        Card ownLibraryCard = new NyssaOfTraken();
        harness.setLibrary(player1, List.of(ownLibraryCard));
        harness.setLibrary(player2, List.of(milledController, milledNyssa));
        harness.setGraveyard(player2, List.of(oldGraveyardCreature));

        harness.setHand(player1, List.of(new TheCyberController()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castArtifact(player1, 0, 3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> cybermen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).toList();
        assertThat(cybermen).hasSize(2);
        assertThat(cybermen).extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(milledController.getId(), milledNyssa.getId());
        for (Permanent cyberman : cybermen) {
            assertThat(gqs.getEffectivePower(gd, cyberman)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, cyberman)).isEqualTo(3);
        }
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(oldGraveyardCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostExcludesSelfNonartifactCreaturesAndOpponentsArtifacts() {
        Permanent controller = harness.addToBattlefieldAndReturn(player1, new TheCyberController());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new CybermanPatrol());
        Permanent ownNonartifact = harness.addToBattlefieldAndReturn(player1, new NyssaOfTraken());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new CybermanPatrol());

        assertThat(gqs.getEffectivePower(gd, controller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, controller)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifact)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, ownNonartifact)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownNonartifact)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentArtifact)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentArtifact)).isEqualTo(2);
    }
}
