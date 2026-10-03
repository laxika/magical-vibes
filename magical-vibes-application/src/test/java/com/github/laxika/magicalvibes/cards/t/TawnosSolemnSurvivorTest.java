package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TawnosSolemnSurvivor.class, GrizzlyBears.class})
class TawnosSolemnSurvivorTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an artifact token and mills two cards")
    void copiesArtifactTokenAndMillsTwo() {
        Permanent tawnos = addReadyTawnos();
        Permanent artifactToken = harness.addToBattlefieldAndReturn(player1, createArtifactToken("Treasure"));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithMultiTargets(player1, battlefieldIndex(tawnos), 0,
                List.of(artifactToken.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("The copy ability cannot target a nontoken artifact")
    void copyAbilityRequiresArtifactToken() {
        Permanent tawnos = addReadyTawnos();
        Card artifact = new Card();
        artifact.setName("Artifact");
        artifact.setType(CardType.ARTIFACT);
        Permanent nontokenArtifact = harness.addToBattlefieldAndReturn(player1, artifact);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, battlefieldIndex(tawnos), 0, List.of(nontokenArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact token");
    }

    @Test
    @DisplayName("Exiles a creature card and creates an artifact token copy after sacrificing two artifact tokens")
    void createsArtifactCopyOfExiledCreature() {
        Permanent tawnos = addReadyTawnos();
        Permanent firstToken = harness.addToBattlefieldAndReturn(player1, createArtifactToken("Treasure 1"));
        Permanent secondToken = harness.addToBattlefieldAndReturn(player1, createArtifactToken("Treasure 2"));
        GrizzlyBears exiledCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(exiledCard));
        addWubManaAndGeneric();

        harness.activateAbility(player1, battlefieldIndex(tawnos), 1, null, null);
        harness.handleMultiplePermanentsChosen(player1, List.of(firstToken.getId(), secondToken.getId()));
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCard);
        Permanent copy = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(copy.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(copy.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(firstToken.getId())
                        || permanent.getId().equals(secondToken.getId()));
    }

    @Test
    @DisplayName("The copy ability is sorcery speed only")
    void copyAbilityIsSorcerySpeedOnly() {
        Permanent tawnos = addReadyTawnos();
        harness.addToBattlefield(player1, createArtifactToken("Treasure 1"));
        harness.addToBattlefield(player1, createArtifactToken("Treasure 2"));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addWubManaAndGeneric();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(tawnos), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyTawnos() {
        Permanent tawnos = harness.addToBattlefieldAndReturn(player1, new TawnosSolemnSurvivor());
        tawnos.setSummoningSick(false);
        return tawnos;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void addWubManaAndGeneric() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Card createArtifactToken(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setToken(true);
        return card;
    }
}
