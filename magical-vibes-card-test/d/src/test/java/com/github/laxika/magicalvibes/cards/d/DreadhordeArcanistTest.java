package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.c.CrushDissent;
import com.github.laxika.magicalvibes.cards.s.SparkHarvest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadhordeArcanist.class, CounselOfTheSoratami.class, GrizzlyBears.class,
        LightningBolt.class, Shock.class, CrushDissent.class, SparkHarvest.class})
class DreadhordeArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Targets only instants and sorceries within its power")
    void targetsCardsWithinPower() {
        Permanent arcanist = addReadyArcanist();
        Card shock = new Shock();
        Card counsel = new CounselOfTheSoratami();
        Card creature = new GrizzlyBears();
        arcanist.setPowerModifier(2);
        harness.setGraveyard(player1, List.of(shock, counsel, creature));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId(), counsel.getId());
    }

    @Test
    @DisplayName("Casts the chosen card for free and exiles it")
    void castsChosenCardForFreeAndExilesIt() {
        Permanent arcanist = addReadyArcanist();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        arcanist.setPowerModifier(2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setGraveyard(player1, List.of(counsel));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Rechecks the source power when the trigger resolves")
    void targetMustStillFitPowerOnResolution() {
        Permanent arcanist = addReadyArcanist();
        Card counsel = new CounselOfTheSoratami();
        arcanist.setPowerModifier(2);
        harness.setGraveyard(player1, List.of(counsel));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        arcanist.setPowerModifier(0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Uses the attacker's last known power if it leaves before resolution")
    void usesLastKnownPowerIfSourceLeaves() {
        Permanent arcanist = addReadyArcanist();
        Card counsel = new CounselOfTheSoratami();
        arcanist.setPowerModifier(2);
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, arcanist.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(counsel.getId()));
    }

    @Test
    @DisplayName("Declining to cast leaves the target in the graveyard")
    void decliningLeavesTargetInGraveyard() {
        addReadyArcanist();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("An uncastable counterspell remains in the graveyard")
    void noLegalSpellTargetsDoesNotExileCard() {
        Permanent arcanist = addReadyArcanist();
        arcanist.setPowerModifier(3);
        CrushDissent dissent = new CrushDissent();
        harness.setGraveyard(player1, List.of(dissent));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(dissent.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(dissent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(dissent);
    }

    @Test
    @DisplayName("Mandatory additional costs still apply to the free spell")
    void cannotCastWhenMandatoryAdditionalCostCannotBePaid() {
        Permanent arcanist = addReadyArcanist();
        SparkHarvest harvest = new SparkHarvest();
        harness.setGraveyard(player1, List.of(harvest));
        harness.addToBattlefield(player2, new DreadhordeArcanist());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(harvest.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, arcanist.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(harvest);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(harvest);
    }

    @Test
    @DisplayName("Last known power reflects changes after attacking")
    void usesChangedPowerAtDepartureRatherThanAttackPower() {
        Permanent arcanist = addReadyArcanist();
        arcanist.setPowerModifier(2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player1, List.of(counsel));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        arcanist.setPowerModifier(0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, arcanist.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(counsel);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(counsel);
    }

    private Permanent addReadyArcanist() {
        Permanent arcanist = harness.addToBattlefieldAndReturn(player1, new DreadhordeArcanist());
        arcanist.setSummoningSick(false);
        return arcanist;
    }

}
