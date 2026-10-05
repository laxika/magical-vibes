package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreasureCruise;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PrimordialMist.class, GrizzlyBears.class, ZoeticCavern.class, TreasureCruise.class})
class PrimordialMistTest extends BaseCardTest {

    @Test
    void mayManifestTopCardAtEndStep() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isManifested())
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exilesFaceDownPermanentAndAllowsPlayingThatCard() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        ZoeticCavern cavernCard = new ZoeticCavern();
        harness.setHand(player1, List.of(cavernCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent faceDownCavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(faceDownCavern.isFaceDown()).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(cavernCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(cavernCard.getId(), player1.getId());

        harness.castFromExile(player1, cavernCard.getId());

        assertThat(gd.findExiledCard(cavernCard.getId())).isNull();
        Permanent playedCavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(playedCavern.isFaceDown()).isFalse();
        assertThat(gqs.isLand(gd, playedCavern)).isTrue();
    }

    @Test
    void mayDeclineManifesting() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        PrimordialMist topCard = new PrimordialMist();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void manifestsNoncreatureCardWithoutAllowingItToTurnFaceUp() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        PrimordialMist topCard = new PrimordialMist();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.addMana(player1, ManaColor.BLUE, 5);

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.isCreature(gd, manifested)).isTrue();
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void exilingManifestedCreaturePaysCostImmediatelyAndStillRequiresManaToCast() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getId()).faceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").isFaceDown()).isFalse();
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void canCastExiledDelveSpellByPayingItsFullManaCost() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        TreasureCruise cruise = new TreasureCruise();
        harness.setLibrary(player1, List.of(cruise));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 8);
        harness.setLibrary(player1, List.of(new PrimordialMist(), new PrimordialMist(), new PrimordialMist()));

        harness.castFromExile(player1, cruise.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(cruise.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cruise);
    }
}
