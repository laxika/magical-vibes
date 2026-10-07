package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.d.DireBlunderbuss;
import com.github.laxika.magicalvibes.cards.d.DireFlail;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.TheGoldenGearColossus;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PestilentCauldron;
import com.github.laxika.magicalvibes.cards.r.RestorativeBurst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TetzinGnomeChampion.class, TheGoldenGearColossus.class, DireFlail.class,
        DireBlunderbuss.class, DarksteelRelic.class, Forest.class, Plains.class,
        PestilentCauldron.class, RestorativeBurst.class, TheMightstoneAndWeakstone.class})
class TetzinGnomeChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Tetzin mills three cards and may return a milled artifact to hand")
    void etbMillsAndReturnsArtifact() {
        Card artifact = new DarksteelRelic();
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), artifact));

        harness.enterBattlefieldAndReturn(player1, new TetzinGnomeChampion());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Darksteel Relic");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Crafting Tetzin with six artifacts returns it transformed")
    void craftReturnsTransformed() {
        Permanent tetzin = addReady(player1, new TetzinGnomeChampion());
        for (int i = 0; i < 6; i++) {
            addReady(player1, new DarksteelRelic());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tetzin);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof TheGoldenGearColossus);
    }

    @Test
    @DisplayName("The Golden-Gear Colossus transforms another double-faced artifact and creates Gnomes when it attacks")
    void backFaceAttackTransformsOtherDoubleFacedArtifactAndCreatesGnomes() {
        Permanent colossus = addTransformedColossus();
        Permanent doubleFacedArtifact = addReady(player1, new DireFlail());
        Permanent ordinaryArtifact = addReady(player1, new DarksteelRelic());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(doubleFacedArtifact.getId()).doesNotContain(ordinaryArtifact.getId());
        harness.handlePermanentChosen(player1, doubleFacedArtifact.getId());
        harness.passBothPriorities();

        assertThat(doubleFacedArtifact.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                p.getCard().isToken() && p.getCard().hasType(CardType.ARTIFACT)
                        && p.getCard().getSubtypes().contains(CardSubtype.GNOME))
                .hasSize(2);
        assertThat(colossus.isTransformed()).isTrue();
    }

    @Test
    void mayLeaveMilledArtifactInGraveyard() {
        Card artifact = new DireFlail();
        harness.setLibrary(player1, List.of(new Forest(), artifact, new Plains()));

        harness.enterBattlefieldAndReturn(player1, new TetzinGnomeChampion());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(artifact);
        harness.assertNotInHand(player1, "Dire Flail");
    }

    @Test
    void returnsAtMostOneOfSeveralMilledArtifacts() {
        Card first = new DireFlail();
        Card second = new DireFlail();
        Card third = new DireFlail();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.enterBattlefieldAndReturn(player1, new TetzinGnomeChampion());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).filteredOn(c ->
                List.of(first, second, third).contains(c)).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void otherDoubleFacedArtifactEntryMillsThree() {
        addReady(player1, new TetzinGnomeChampion());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Forest(), new Plains()));

        harness.enterBattlefieldAndReturn(player1, new DireFlail());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void modalDoubleFacedArtifactEntryAlsoTriggersMill() {
        addReady(player1, new TetzinGnomeChampion());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new PestilentCauldron());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void meldArtifactEntryAlsoTriggersMill() {
        addReady(player1, new TetzinGnomeChampion());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Forest(),
                new Plains(), new Forest(), new Plains(), new Forest(), new Plains()));
        harness.setHand(player1, List.of(new TheMightstoneAndWeakstone()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void shortLibraryOnlyOffersArtifactsMilledByThisAbility() {
        Card previouslyInGraveyard = new DireFlail();
        harness.setGraveyard(player1, List.of(previouslyInGraveyard));
        harness.setLibrary(player1, List.of(new Forest(), new Plains()));

        harness.enterBattlefieldAndReturn(player1, new TetzinGnomeChampion());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3).contains(previouslyInGraveyard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotInHand(player1, "Dire Flail");
    }

    @Test
    void ordinaryArtifactAndOpponentsDoubleFacedArtifactDoNotTriggerMill() {
        addReady(player1, new TetzinGnomeChampion());
        harness.setLibrary(player1, List.of(new Forest(), new Plains(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.enterBattlefieldAndReturn(player2, new DireFlail());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    void craftAcceptsMixedBattlefieldAndGraveyardMaterialsAndCreatesGnomes() {
        Permanent tetzin = addReady(player1, new TetzinGnomeChampion());
        for (int i = 0; i < 3; i++) {
            addReady(player1, new DireFlail());
        }
        List<Card> graveyardArtifacts = List.of(new DireFlail(), new DireFlail(), new DireFlail());
        harness.setGraveyard(player1, graveyardArtifacts);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(tetzin.getOriginalCard().getId())).isNotNull();
        for (Card artifact : graveyardArtifacts) {
            assertThat(gd.findExiledCard(artifact.getId())).isNotNull();
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "The Golden-Gear Colossus");
        assertGnomes(2);
        assertThat(gd.findExiledCard(tetzin.getOriginalCard().getId())).isNull();
    }

    @Test
    void craftRequiresSixOtherArtifacts() {
        Permanent tetzin = addReady(player1, new TetzinGnomeChampion());
        for (int i = 0; i < 5; i++) {
            addReady(player1, new DireFlail());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(6).contains(tetzin);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftedColossusTransformsAnotherArtifactOnEntry() {
        addReady(player1, new TetzinGnomeChampion());
        Permanent artifact = addReady(player1, new DireFlail());
        List<Card> materials = List.of(new DireFlail(), new DireFlail(), new DireFlail(),
                new DireFlail(), new DireFlail(), new DireFlail());
        harness.setGraveyard(player1, materials);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.handleMultipleCardsChosen(player1, materials.stream().map(Card::getId).toList());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTransformed()).isTrue();
        harness.assertOnBattlefield(player1, "The Golden-Gear Colossus");
        assertGnomes(2);
    }

    @Test
    void craftCannotBeActivatedOnOpponentsTurn() {
        addReady(player1, new TetzinGnomeChampion());
        harness.setGraveyard(player1, List.of(new DireFlail(), new DireFlail(), new DireFlail(),
                new DireFlail(), new DireFlail(), new DireFlail()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tetzin, Gnome Champion");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void attackMayChooseNoTargetAndStillCreatesGnomes() {
        Permanent colossus = addTransformedColossus();
        Permanent artifact = addReady(player1, new DireFlail());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTransformed()).isFalse();
        assertThat(colossus.isTapped()).isFalse();
        assertGnomes(2);
    }

    @Test
    void attackCanTransformAnArtifactBackToItsFrontFace() {
        Permanent colossus = addTransformedColossus();
        DireFlail front = new DireFlail();
        Permanent artifact = addReady(player1, front);
        artifact.setCard(front.getBackFaceCard());
        artifact.setTransformed(true);
        Permanent opponentArtifact = addReady(player2, new DireFlail());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(artifact.getId())
                .doesNotContain(colossus.getId(), opponentArtifact.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Dire Flail");
        assertGnomes(2);
    }

    @Test
    void modalDoubleFacedArtifactIsLegalTargetButCannotTransform() {
        addTransformedColossus();
        Permanent cauldron = addReady(player1, new PestilentCauldron());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(cauldron.getId());
        harness.handlePermanentChosen(player1, cauldron.getId());
        harness.passBothPriorities();

        assertThat(cauldron.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "Pestilent Cauldron");
        assertGnomes(2);
    }

    @Test
    void meldArtifactIsLegalTargetButCannotTransform() {
        addTransformedColossus();
        Permanent meldArtifact = addReady(player1, new TheMightstoneAndWeakstone());
        addReady(player1, new DireFlail());

        declareAttackers(player1, List.of(0));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(meldArtifact.getId());
        harness.handlePermanentChosen(player1, meldArtifact.getId());
        harness.passBothPriorities();

        assertThat(meldArtifact.isTransformed()).isFalse();
        harness.assertOnBattlefield(player1, "The Mightstone and Weakstone");
        assertGnomes(2);
    }

    private void assertGnomes(int count) {
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p ->
                p.getCard().isToken() && p.getCard().hasType(CardType.ARTIFACT)
                        && p.getCard().getSubtypes().contains(CardSubtype.GNOME))
                .hasSize(count).allSatisfy(p -> {
                    assertThat(p.getEffectivePower()).isEqualTo(1);
                    assertThat(p.getEffectiveToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isNull();
                });
    }

    private Permanent addTransformedColossus() {
        TetzinGnomeChampion front = new TetzinGnomeChampion();
        Permanent colossus = harness.addToBattlefieldAndReturn(player1, front);
        colossus.setCard(front.getBackFaceCard());
        colossus.setTransformed(true);
        colossus.setSummoningSick(false);
        return colossus;
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
