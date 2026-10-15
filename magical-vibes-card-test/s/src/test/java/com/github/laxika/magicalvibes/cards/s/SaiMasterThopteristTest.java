package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaiMasterThopterist.class, Memnite.class, GrizzlyBears.class})
class SaiMasterThopteristTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an artifact spell creates a 1/1 Thopter token with flying")
    void artifactCastCreatesThopter() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities(); // resolve the trigger and the artifact

        List<Permanent> tokens = findPermanents(player1, "Thopter");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Casting a nonartifact spell creates no Thopter")
    void nonArtifactCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing two artifacts draws a card")
    void sacrificeTwoArtifactsDrawsCard() {
        Permanent sai = harness.addToBattlefieldAndReturn(player1, new SaiMasterThopterist());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player1, new Memnite());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, indexOf(sai), null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Memnite"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Cannot activate the draw ability without two artifacts to sacrifice")
    void cannotActivateWithoutTwoArtifacts() {
        Permanent sai = harness.addToBattlefieldAndReturn(player1, new SaiMasterThopterist());
        harness.addToBattlefield(player1, new Memnite());

        harness.addMana(player1, ManaColor.BLUE, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sai), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
    }

    @Test
    @DisplayName("Putting an artifact onto the battlefield without casting does not trigger Sai")
    void artifactEnteringWithoutCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.enterBattlefieldAndReturn(player1, new Memnite());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's artifact spell does not trigger Sai")
    void opponentsArtifactDoesNotTrigger() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Memnite()));

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(findPermanents(player2, "Thopter")).isEmpty();
    }

    @Test
    @DisplayName("The Thopter trigger resolves before the artifact spell")
    void thopterCreatedBeforeArtifactResolves() {
        harness.addToBattlefield(player1, new SaiMasterThopterist());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);
        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Memnite");
        Permanent token = findPermanent(player1, "Thopter");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).isEmpty();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Memnite");
        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
    }

    @Test
    @DisplayName("Thopter tokens can pay the sacrifice cost and are sacrificed before drawing")
    void tokensPayCostBeforeDrawResolves() {
        Permanent sai = harness.addToBattlefieldAndReturn(player1, new SaiMasterThopterist());
        harness.setHand(player1, List.of(new Memnite(), new Memnite()));
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        List<Permanent> tokens = findPermanents(player1, "Thopter");
        assertThat(tokens).hasSize(2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, indexOf(sai), null, null);
        harness.handlePermanentChosen(player1, tokens.get(0).getId());
        harness.handlePermanentChosen(player1, tokens.get(1).getId());

        assertThat(findPermanents(player1, "Thopter")).isEmpty();
        assertThat(findPermanents(player1, "Memnite")).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Opposing artifacts cannot pay Sai's sacrifice cost")
    void opponentsArtifactsCannotPayCost() {
        Permanent sai = harness.addToBattlefieldAndReturn(player1, new SaiMasterThopterist());
        harness.addToBattlefield(player1, new Memnite());
        harness.addToBattlefield(player2, new Memnite());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(sai), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough permanents to sacrifice");
        harness.assertOnBattlefield(player1, "Memnite");
        harness.assertOnBattlefield(player2, "Memnite");
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
