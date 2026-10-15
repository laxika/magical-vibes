package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrookOfCondemnation.class, GrizzlyBears.class, Shock.class})
class CrookOfCondemnationTest extends BaseCardTest {

    @Test
    @DisplayName("{1}, {T}: exiles target card from a graveyard")
    void tapExilesTargetGraveyardCard() {
        Permanent crook = addReadyCrook(player1);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);
        harness.activateAbility(player1, crookIndex, 0, null, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crook);
        assertThat(crook.isTapped()).isTrue();
    }

    @Test
    @DisplayName("{1}, {T}: can exile from own graveyard")
    void tapExilesFromOwnGraveyard() {
        Permanent crook = addReadyCrook(player1);
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);
        harness.activateAbility(player1, crookIndex, 0, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("{1}, {T}: rejects a target not in any graveyard")
    void rejectsTargetNotInGraveyard() {
        Permanent crook = addReadyCrook(player1);
        Card bears = new GrizzlyBears();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, crookIndex, 0, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("{1}, Exile self: exile all graveyards")
    void exileSelfExilesAllGraveyards() {
        Permanent crook = addReadyCrook(player1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int crookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crook);
        harness.activateAbility(player1, crookIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crook);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Crook of Condemnation"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    void newlyEnteredArtifactCanPayTapCost() {
        Permanent crook = harness.addToBattlefieldAndReturn(player1, new CrookOfCondemnation());
        Card target = new CrookOfCondemnation();
        Card other = new CrookOfCondemnation();
        harness.setGraveyard(player2, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);

        assertThat(crook.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target, other);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    void tapAbilityRequiresTarget() {
        Permanent crook = addReadyCrook(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, null, Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crook.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedArtifactCannotPayTapCost() {
        Permanent crook = addReadyCrook(player1);
        crook.tap();
        Card target = new CrookOfCondemnation();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothAbilitiesRequireMana() {
        Permanent crook = addReadyCrook(player1);
        Card target = new CrookOfCondemnation();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(crook.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crook);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedArtifactExilesItselfAsCostBeforeClearingGraveyards() {
        Permanent crook = addReadyCrook(player1);
        crook.tap();
        Card ownCard = new CrookOfCondemnation();
        Card opposingCard = new CrookOfCondemnation();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crook);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(crook.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(crook.getCard(), ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingCard);
    }

    @Test
    void exileAllCanBeActivatedWithEmptyGraveyards() {
        Permanent crook = addReadyCrook(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(crook.getCard());
    }

    @Test
    void targetExiledInResponseDoesNotExileAnotherCard() {
        addReadyCrook(player1);
        addReadyCrook(player2);
        Card target = new CrookOfCondemnation();
        Card other = new CrookOfCondemnation();
        harness.setGraveyard(player2, List.of(target, other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player2, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    @Test
    void exileAllIncludesCardsThatEnterGraveyardsInResponse() {
        Permanent crook = addReadyCrook(player1);
        Card ownCard = new CrookOfCondemnation();
        Card opposingCard = new CrookOfCondemnation();
        Shock response = new Shock();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.setHand(player2, List.of(response));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard, response);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(crook.getCard(), ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(opposingCard, response);
    }
    private Permanent addReadyCrook(Player player) {
        return addCreatureReady(player, new CrookOfCondemnation());
    }
}
