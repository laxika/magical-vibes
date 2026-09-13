package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({BragoKingEternal.class, Forest.class, GrizzlyBears.class, LayClaim.class, NullRod.class})
class BragoKingEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets you choose any number of your nonland permanents to flicker")
    void flickersChosenPermanents() {
        Permanent brago = addReadyCreature(player1, new BragoKingEternal());
        brago.setAttacking(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(bears.getId(), nullRod.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, nullRod.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Null Rod");
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
        assertThat(findPermanent(player1, "Null Rod").getId()).isNotEqualTo(nullRod.getId());
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing no permanents is allowed")
    void mayChooseNoPermanents() {
        Permanent brago = addReadyCreature(player1, new BragoKingEternal());
        brago.setAttacking(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(bears.getId());
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gameData = harness.getGameData();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gameData.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
    @Test
    @DisplayName("Combat damage flickers any number of targeted nonland permanents")
    void flickersSelectedNonlandPermanents() {
        Permanent brago = addAttackingBrago();
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, firstBear.getId());
        harness.handlePermanentChosen(player1, secondBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .hasSize(2)
                .extracting(Permanent::getId)
                .doesNotContain(firstBear.getId(), secondBear.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brago);
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger only offers controlled nonland permanents")
    void onlyOffersControlledNonlandPermanents() {
        Permanent brago = addAttackingBrago();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombatToTargetChoice();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds())
                .contains(brago.getId(), ownBear.getId())
                .doesNotContain(ownForest.getId(), opponentBear.getId());
    }

    @Test
    @DisplayName("Flickered permanents return under their owners' control")
    void returnsUnderOwnersControl() {
        Permanent stolenBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, stolenBear.getId());
        harness.passBothPriorities();

        Permanent brago = addAttackingBrago();
        Permanent controlledBear = gqs.findPermanentById(
                gd, harness.getPermanentId(player1, "Grizzly Bears"));

        resolveCombatToTargetChoice();
        harness.handlePermanentChosen(player1, controlledBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GrizzlyBears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(brago);
    }

    private Permanent addAttackingBrago() {
        Permanent brago = harness.addToBattlefieldAndReturn(player1, new BragoKingEternal());
        brago.setSummoningSick(false);
        brago.setAttacking(true);
        return brago;
    }

    private void resolveCombatToTargetChoice() {
        harness.forceActivePlayer(player1);
        resolveCombat();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
