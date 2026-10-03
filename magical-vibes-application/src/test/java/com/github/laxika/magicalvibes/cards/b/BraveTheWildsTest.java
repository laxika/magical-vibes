package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CandyTrail;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.u.UpTheBeanstalk;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraveTheWilds.class, CandyTrail.class, Forest.class, UpTheBeanstalk.class})
class BraveTheWildsTest extends BaseCardTest {

    @Test
    void withoutBargainSearchesForABasicLand() {
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new BraveTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void bargainAnimatesTargetLandAndSearchesForABasicLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new BraveTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, land.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveCardTypes(gd, land)).contains(CardType.LAND);
        harness.assertInGraveyard(player1, "Candy Trail");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void bargainAnimationPersistsAfterEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, land.getId(), sacrifice.getId());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
    }

    @Test
    void bargainedSpellCannotTargetAnOpponentLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CandyTrail());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bargainCanSacrificeAnEnchantmentAndAnimateWithAnEmptyLibrary() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new UpTheBeanstalk());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, land.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Up the Beanstalk");
        harness.assertInGraveyard(player1, "Brave the Wilds");
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
    }

    @Test
    void bargainCanSacrificeATokenThatIsNeitherArtifactNorEnchantment() {
        Forest token = new Forest();
        token.setToken(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, token);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, land.getId(), sacrifice.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void bargainCannotSacrificeANontokenLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castKickedSorceryWithSacrificeNoKickerTarget(
                player1, 0, land.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void sacrificingTheTargetTokenLandMakesTheWholeSpellFailToResolve() {
        Forest token = new Forest();
        token.setToken(true);
        Permanent land = harness.addToBattlefieldAndReturn(player1, token);
        Forest libraryLand = new Forest();
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(libraryLand));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castKickedSorceryWithSacrificeNoKickerTarget(player1, 0, land.getId(), land.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Brave the Wilds");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayFailToFindEvenWhenABasicLandIsAvailable() {
        Forest libraryLand = new Forest();
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(libraryLand));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertNotInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Brave the Wilds");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryLand);
    }
}
